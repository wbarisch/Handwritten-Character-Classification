package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class BitmapUtils {

    public static Bitmap centerAndResizeBitmap(Bitmap bitmap, int desiredSize) {
        int margin = 2;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;

        // Find the bounding box of non-black pixels (white characters)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[x + y * width];

                // Since characters are white and background is black
                if (pixel != Color.BLACK) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (right < left || bottom < top) {
            // No non-black pixels found, return an empty bitmap with a black background
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            Canvas emptyCanvas = new Canvas(emptyBitmap);
            emptyCanvas.drawColor(Color.BLACK);
            return emptyBitmap;
        }

        int contentWidth = right - left + 1;
        int contentHeight = bottom - top + 1;

        // Crop the bitmap to the bounding box
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, left, top, contentWidth, contentHeight);

        // Now scale and center the cropped bitmap into the desired size
        float scale = Math.min(
                (desiredSize - 2 * margin) / (float) contentWidth,
                (desiredSize - 2 * margin) / (float) contentHeight
        );
        float scaledWidth = contentWidth * scale;
        float scaledHeight = contentHeight * scale;

        Bitmap outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.BLACK); // Set background color to black

        float dx = (desiredSize - scaledWidth) / 2f;
        float dy = (desiredSize - scaledHeight) / 2f;
        RectF destRect = new RectF(dx, dy, dx + scaledWidth, dy + scaledHeight);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);

        // Draw the scaled character onto the output bitmap
        canvas.drawBitmap(croppedBitmap, null, destRect, paint);

        return outputBitmap;
    }
}