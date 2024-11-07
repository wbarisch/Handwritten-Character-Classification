package com.example.hcc_elektrobit;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class BitmapUtils {


    public static Bitmap drawPathToBitmap(Path path, int canvasWidth, int canvasHeight, int desiredSize, float strokeWidth, boolean antiAlias) {
        // Calculate scaling factors to map canvas to bitmap dimensions
        float margin = desiredSize * 0.05f; // You can adjust this as needed (e.g., 5% of desiredSize)
        float scaleX = (desiredSize - 2 * margin) / canvasWidth;
        float scaleY = (desiredSize - 2 * margin) / canvasHeight;
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio

        // Create matrix to scale the path
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Transform the path with scaling
        Path scaledPath = new Path();
        path.transform(matrix, scaledPath);

        // Get the bounds of the scaled path
        RectF bounds = new RectF();
        scaledPath.computeBounds(bounds, true);

        // Compute the translation to center the path within the desiredSize, considering margin
        float dx = ((desiredSize - 2 * margin - bounds.width()) / 2f) + margin - bounds.left;
        float dy = ((desiredSize - 2 * margin - bounds.height()) / 2f) + margin - bounds.top;

        matrix.postTranslate(dx, dy); // Update the existing matrix to include translation

        // Transform the path with the updated matrix
        Path transformedPath = new Path();
        path.transform(matrix, transformedPath);

        // Draw the transformed path onto bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setAntiAlias(antiAlias);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth); // Use fixed stroke width
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

        float scalingFactorWidth = (desiredSize - 2 * margin) / (float) contentWidth;
        float scalingFactorHeight = (desiredSize - 2 * margin) / (float) contentHeight;
        float scale = Math.min(1.0f, Math.min(scalingFactorWidth, scalingFactorHeight));

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