package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class ImageProcessingHelper {

    public static List<Bitmap> processAndSegmentWord(Bitmap bitmap, boolean saveAsWhiteCharacterOnBlack, int bitmapSize) {
        List<Bitmap> extractedBitmaps = new ArrayList<>();

        Bitmap adjustedBitmap = adjustBitmapColors(bitmap, saveAsWhiteCharacterOnBlack);
        Mat mat = new Mat();
        Utils.bitmapToMat(adjustedBitmap, mat);

        if (mat.channels() > 1) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        }
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        boolean invertedForProcessing = false;
        if (!saveAsWhiteCharacterOnBlack) {
            Core.bitwise_not(mat, mat);
            invertedForProcessing = true;
        }

        int padding = 5;
        Core.copyMakeBorder(mat, mat, padding, padding, padding, padding, Core.BORDER_CONSTANT, new Scalar(0));

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return extractedBitmaps;
        }

        List<Rect> boundingRects = new ArrayList<>();
        int imageArea = mat.rows() * mat.cols();
        double minContourArea = imageArea * 0.0001;

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > minContourArea && contourArea < imageArea * 0.9) {
                boundingRects.add(rect);
            }
        }

        if (boundingRects.isEmpty()) {
            return extractedBitmaps;
        }

        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (Rect rect : boundingRects) {
            if (rect.x < left) left = rect.x;
            if (rect.y < top) top = rect.y;
            if (rect.x + rect.width > right) right = rect.x + rect.width;
            if (rect.y + rect.height > bottom) bottom = rect.y + rect.height;
        }

        int expansion = 10;
        left = Math.max(left - expansion, 0);
        top = Math.max(top - expansion, 0);
        right = Math.min(right + expansion, mat.cols());
        bottom = Math.min(bottom + expansion, mat.rows());

        Rect writingArea = new Rect(left, top, right - left, bottom - top);
        mat = new Mat(mat, writingArea);

        for (int i = 0; i < boundingRects.size(); i++) {
            Rect rect = boundingRects.get(i);
            rect.x -= left;
            rect.y -= top;
            boundingRects.set(i, rect);
        }
        List<Integer> heights = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        for (Rect rect : boundingRects) {
            heights.add(rect.height);
            widths.add(rect.width);
        }
        Collections.sort(heights);
        Collections.sort(widths);
        int medianIndex = heights.size() / 2;
        double medianHeight = heights.get(medianIndex);
        double medianWidth = widths.get(medianIndex);

        double dotSizeThreshold = medianHeight * 0.3;

        List<Rect> mainBodies = new ArrayList<>();
        List<Rect> dots = new ArrayList<>();

        for (Rect rect : boundingRects) {
            double aspectRatio = (double) rect.width / rect.height;
            if (rect.height < dotSizeThreshold && rect.width < dotSizeThreshold && aspectRatio >= 0.5 && aspectRatio <= 1.5) {
                dots.add(rect);
            } else {
                mainBodies.add(rect);
            }
        }

        Map<Integer, Rect> mergedRectsMap = new HashMap<>();
        for (int i = 0; i < mainBodies.size(); i++) {
            mergedRectsMap.put(i, mainBodies.get(i));
        }
        boolean[] dotMerged = new boolean[dots.size()];

        for (int i = 0; i < dots.size(); i++) {
            Rect dotRect = dots.get(i);
            int dotCenterX = dotRect.x + dotRect.width / 2;

            double minDistance = Double.MAX_VALUE;
            int bestIndex = -1;

            for (int j = 0; j < mainBodies.size(); j++) {
                Rect mainRect = mainBodies.get(j);

                double verticalGap = mainRect.y - (dotRect.y + dotRect.height);
                double verticalGapThreshold = mainRect.height * 1.5;

                if (verticalGap >= -mainRect.height * 0.1 && verticalGap <= verticalGapThreshold) {
                    int mainCenterX = mainRect.x + mainRect.width / 2;
                    int horizontalDistance = Math.abs(mainCenterX - dotCenterX);
                    int maxHorizontalDistance = (int) (mainRect.width * 1.5);

                    if (horizontalDistance <= maxHorizontalDistance) {
                        double distance = Math.hypot(horizontalDistance, verticalGap);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestIndex = j;
                        }
                    }
                }
            }

            if (bestIndex != -1) {
                Rect mainRect = mergedRectsMap.get(bestIndex);
                Rect combinedRect = unionRect(mainRect, dotRect);
                mergedRectsMap.put(bestIndex, combinedRect);
                dotMerged[i] = true;
            }
        }

        List<Rect> mergedRects = new ArrayList<>(mergedRectsMap.values());

        for (int i = 0; i < dots.size(); i++) {
            if (!dotMerged[i]) {
                mergedRects.add(dots.get(i));
            }
        }
        mergedRects = mergeNearbyComponents(mergedRects);

        drawBoundingRects(mat, mergedRects, "Merged Bounding Rects");

        int totalHeight = 0;
        for (Rect rect : mergedRects) {
            totalHeight += rect.height;
        }
        double avgCharHeight = (double) totalHeight / mergedRects.size();

        List<List<Rect>> lines = new ArrayList<>();
        mergedRects.sort(Comparator.comparingInt(r -> r.y));
        double lineThreshold = avgCharHeight * 0.7;

        List<Rect> currentLine = new ArrayList<>();
        Rect previousRect = null;
        for (Rect rect : mergedRects) {
            if (previousRect == null) {
                currentLine.add(rect);
            } else {
                if (Math.abs(rect.y - previousRect.y) <= lineThreshold) {
                    currentLine.add(rect);
                } else {
                    lines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentLine.add(rect);
                }
            }
            previousRect = rect;
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        List<Rect> sortedRects = new ArrayList<>();
        for (List<Rect> line : lines) {
            line.sort(Comparator.comparingInt(r -> r.x));
            sortedRects.addAll(line);
        }

        for (Rect rect : sortedRects) {
            int adjustedX = rect.x;
            int adjustedY = rect.y;
            int adjustedWidth = rect.width;
            int adjustedHeight = rect.height;

            if (adjustedX + adjustedWidth > mat.cols()) {
                adjustedWidth = mat.cols() - adjustedX;
            }
            if (adjustedY + adjustedHeight > mat.rows()) {
                adjustedHeight = mat.rows() - adjustedY;
            }

            Rect adjustedRect = new Rect(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
            Mat charMat = new Mat(mat, adjustedRect);

            if (invertedForProcessing) {
                Core.bitwise_not(charMat, charMat);
            }

            Bitmap centeredBitmap = centerAndResizeCharMat(charMat, bitmapSize);
            extractedBitmaps.add(centeredBitmap);
        }

        return extractedBitmaps;
    }

    private static Bitmap adjustBitmapColors(Bitmap originalBitmap, boolean saveAsWhiteCharacterOnBlack) {
        Bitmap adjustedBitmap = Bitmap.createBitmap(
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                originalBitmap.getConfig()
        );

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        int[] pixels = new int[width * height];
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            if (saveAsWhiteCharacterOnBlack) {
                int alpha = Color.alpha(color);
                int red = 255 - Color.red(color);
                int green = 255 - Color.green(color);
                int blue = 255 - Color.blue(color);
                pixels[i] = Color.argb(alpha, red, green, blue);
            } else {
                pixels[i] = color;
            }
        }

        adjustedBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return adjustedBitmap;
    }

    private static List<Rect> mergeNearbyComponents(List<Rect> rects) {
        List<Integer> heights = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        for (Rect rect : rects) {
            heights.add(rect.height);
            widths.add(rect.width);
        }
        Collections.sort(heights);
        Collections.sort(widths);
        double medianHeight = heights.get(heights.size() / 2);
        double medianWidth = widths.get(widths.size() / 2);

        double maxHorizontalGap = medianWidth * 0.15;
        double maxVerticalGap = medianHeight * 0.15;

        boolean[] merged = new boolean[rects.size()];
        List<Rect> mergedRects = new ArrayList<>();

        for (int i = 0; i < rects.size(); i++) {
            if (merged[i]) continue;

            Rect combinedRect = new Rect(rects.get(i).x, rects.get(i).y, rects.get(i).width, rects.get(i).height);
            merged[i] = true;

            boolean mergedAny;
            do {
                mergedAny = false;
                for (int j = 0; j < rects.size(); j++) {
                    if (merged[j]) continue;
                    Rect compareRect = rects.get(j);

                    if (areRectsClose(combinedRect, compareRect, maxHorizontalGap, maxVerticalGap)) {
                        Rect tempCombinedRect = unionRect(combinedRect, compareRect);

                        double maxAllowedWidth = medianWidth * 1.5;
                        double maxAllowedHeight = medianHeight * 2.0;
                        if (tempCombinedRect.width <= maxAllowedWidth && tempCombinedRect.height <= maxAllowedHeight) {
                            combinedRect = tempCombinedRect;
                            merged[j] = true;
                            mergedAny = true;
                            break;
                        }
                    }
                }
            } while (mergedAny);

            mergedRects.add(combinedRect);
        }
        return mergedRects;
    }
    private static boolean areRectsClose(Rect r1, Rect r2, double maxHGap, double maxVGap) {
        int hDistance = Math.abs((r1.x + r1.width / 2) - (r2.x + r2.width / 2)) - (r1.width + r2.width) / 2;
        int vDistance = Math.abs((r1.y + r1.height / 2) - (r2.y + r2.height / 2)) - (r1.height + r2.height) / 2;

        return hDistance <= maxHGap && vDistance <= maxVGap;
    }

    private static Rect unionRect(Rect rectA, Rect rectB) {
        int x = Math.min(rectA.x, rectB.x);
        int y = Math.min(rectA.y, rectB.y);
        int width = Math.max(rectA.x + rectA.width, rectB.x + rectB.width) - x;
        int height = Math.max(rectA.y + rectA.height, rectB.y + rectB.height) - y;
        return new Rect(x, y, width, height);
    }

    private static void drawBoundingRects(Mat mat, List<Rect> rects, String windowName) {
        Mat matCopy = mat.clone();

        for (Rect rect : rects) {
            Imgproc.rectangle(matCopy, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
        }

        Bitmap bitmap = Bitmap.createBitmap(matCopy.cols(), matCopy.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matCopy, bitmap);

    }

    private static Bitmap centerAndResizeCharMat(Mat charMat, int desiredSize) {
        Mat nonZeroCoordinates = new Mat();
        Core.findNonZero(charMat, nonZeroCoordinates);

        if (nonZeroCoordinates.empty()) {
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            Canvas emptyCanvas = new Canvas(emptyBitmap);
            emptyCanvas.drawColor(Color.BLACK);
            return emptyBitmap;
        }

        Rect bbox = Imgproc.boundingRect(nonZeroCoordinates);
        Mat cropped = new Mat(charMat, bbox);

        int contentWidth = cropped.cols();
        int contentHeight = cropped.rows();
        int margin = 2;
        int maxContentSize = desiredSize - 2 * margin;
        float scale = (float) maxContentSize / Math.max(contentWidth, contentHeight);
        scale = Math.min(scale, 1.0f);

        int newWidth = Math.round(contentWidth * scale);
        int newHeight = Math.round(contentHeight * scale);
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        Size newSize = new Size(newWidth, newHeight);
        Mat resizedChar = new Mat();
        Imgproc.resize(cropped, resizedChar, newSize, 0, 0, Imgproc.INTER_AREA);

        Mat outputMat = Mat.zeros(desiredSize, desiredSize, charMat.type());

        int x = (desiredSize - newWidth) / 2;
        int y = (desiredSize - newHeight) / 2;
        Rect roi = new Rect(x, y, newWidth, newHeight);
        resizedChar.copyTo(outputMat.submat(roi));

        Bitmap centeredBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, centeredBitmap);
        return centeredBitmap;
    }
}
