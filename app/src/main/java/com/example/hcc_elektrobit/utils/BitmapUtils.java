package com.example.hcc_elektrobit.utils;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class BitmapUtils {


    public static Bitmap drawPathToBitmap(Path path, int canvasWidth, int canvasHeight, int desiredSize, float strokeWidth, boolean antiAlias) {

        float margin = desiredSize * 0.05f;
        float scaleX = (desiredSize - 2 * margin) / canvasWidth;
        float scaleY = (desiredSize - 2 * margin) / canvasHeight;
        float scale = Math.min(scaleX, scaleY);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Path scaledPath = new Path();
        path.transform(matrix, scaledPath);
        RectF bounds = new RectF();
        scaledPath.computeBounds(bounds, true);

        float dx = ((desiredSize - 2 * margin - bounds.width()) / 2f) + margin - bounds.left;
        float dy = ((desiredSize - 2 * margin - bounds.height()) / 2f) + margin - bounds.top;

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


}