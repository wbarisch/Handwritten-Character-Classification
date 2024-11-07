package com.example.hcc_elektrobit;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class BitmapUtils {


    public static Bitmap drawPathToBitmap(Path path, int desiredSize, float strokeWidth, boolean antiAlias) {

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        if (bounds.isEmpty()) {
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            Canvas emptyCanvas = new Canvas(emptyBitmap);
            emptyCanvas.drawColor(Color.WHITE);
            return emptyBitmap;
        }

        float margin = 2f;

        float scale = Math.min(
                (desiredSize - 2 * margin) / bounds.width(),
                (desiredSize - 2 * margin) / bounds.height()
        );
          // Find bounding box of non-background content
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        float dx = (desiredSize - bounds.width() * scale) / 2f - bounds.left * scale;
        float dy = (desiredSize - bounds.height() * scale) / 2f - bounds.top * scale;
        matrix.postTranslate(dx, dy);
        Path transformedPath = new Path();
        path.transform(matrix, transformedPath);
        Bitmap outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setAntiAlias(antiAlias);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.BLACK);
        canvas.drawPath(transformedPath, paint);

        return outputBitmap;
    }

    public static Bitmap centerAndResizeBitmap(Bitmap bitmap, int desiredSize, boolean antiAlias) {
        int margin = 2;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int backgroundColor = pixels[0];

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[x + y * width];

                if (pixel != backgroundColor) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (right < left || bottom < top) {
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            Canvas emptyCanvas = new Canvas(emptyBitmap);
            emptyCanvas.drawColor(backgroundColor);
            return emptyBitmap;
        }

        int contentWidth = right - left + 1;
        int contentHeight = bottom - top + 1;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, left, top, contentWidth, contentHeight);

        float scale = Math.min(
                (desiredSize - 2 * margin) / (float) contentWidth,
                (desiredSize - 2 * margin) / (float) contentHeight
        );
        float scaledWidth = contentWidth * scale;
        float scaledHeight = contentHeight * scale;

        Bitmap outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(backgroundColor);

        float dx = (desiredSize - scaledWidth) / 2f;
        float dy = (desiredSize - scaledHeight) / 2f;
        RectF destRect = new RectF(dx, dy, dx + scaledWidth, dy + scaledHeight);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(antiAlias);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawBitmap(croppedBitmap, null, destRect, paint);

        return outputBitmap;
    }

    public static Bitmap centerAndResizeBitmapFixedSize(Bitmap bitmap, boolean antiAlias) {
        int desiredSize = 105;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int backgroundColor = pixels[0];

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[x + y * width];
                if (pixel != backgroundColor) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (right < left || bottom < top) {
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            emptyBitmap.eraseColor(backgroundColor);
            return emptyBitmap;
        }

        int contentWidth = right - left + 1;
        int contentHeight = bottom - top + 1;
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, left, top, contentWidth, contentHeight);

        float scale = Math.min(desiredSize / (float) contentWidth, desiredSize / (float) contentHeight);
        float scaledWidth = contentWidth * scale;
        float scaledHeight = contentHeight * scale;
        Bitmap outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(backgroundColor);
        float dx = (desiredSize - scaledWidth) / 2f;
        float dy = (desiredSize - scaledHeight) / 2f;

        RectF destRect = new RectF(dx, dy, dx + scaledWidth, dy + scaledHeight);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(antiAlias);

        canvas.drawBitmap(croppedBitmap, null, destRect, paint);

        return outputBitmap;
    }


}