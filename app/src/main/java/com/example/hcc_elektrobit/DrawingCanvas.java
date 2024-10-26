package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class DrawingCanvas extends View {

    Paint paint;
    Path path;

    private boolean useOldBitmapMethod = false;

    public DrawingCanvas(Context context, AttributeSet attributeSet){

        super(context, attributeSet);
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(30f); //original was 100f

    }

    public boolean isUseOldBitmapMethod() {
        return this.useOldBitmapMethod;
    }


    public void setUseOldBitmapMethod(boolean useOldBitmapMethod) {
        this.useOldBitmapMethod = useOldBitmapMethod;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas){
        super.onDraw(canvas);
        canvas.drawPath(path,paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos, yPos);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos, yPos);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean performClick() {

        return super.performClick();
    }

    public void clear(){

        path.reset();
        invalidate();

    }

    /*TODO: Implement a boolean that disables centering and matrix operations for testing.
    Have it be a toggle one the main screen to toggle between original method of getBitmap and my method of getBitmap
    ,adding a toggle for setting Antialias to true or false. Add a toggle for different stroke widths*/
    public Bitmap getBitmap(int dims) {
        if (useOldBitmapMethod) {
            // Old method
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            this.draw(canvas);
            return Bitmap.createScaledBitmap(bitmap, dims, dims, true);
        } else {

            int margin = 2;
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            if (bounds.isEmpty() || bounds.width() <= 0 || bounds.height() <= 0) {
                Bitmap emptyBitmap = Bitmap.createBitmap(dims, dims, Bitmap.Config.ARGB_8888);
                Canvas emptyCanvas = new Canvas(emptyBitmap);
                emptyCanvas.drawColor(Color.WHITE);
                return emptyBitmap;
            }

            float strokeWidth = paint.getStrokeWidth();
            float halfStrokeWidth = strokeWidth / 2f;
            bounds.inset(-halfStrokeWidth, -halfStrokeWidth);

            float contentWidth = bounds.width();
            float contentHeight = bounds.height();

            float targetSize = dims - 2 * margin;
            float scale = Math.min(targetSize / contentWidth, targetSize / contentHeight);

            Bitmap finalBitmap = Bitmap.createBitmap(dims, dims, Bitmap.Config.ARGB_8888);
            Canvas finalCanvas = new Canvas(finalBitmap);
            finalCanvas.drawColor(Color.WHITE);

            Matrix matrix = new Matrix();
            matrix.postTranslate(-bounds.left, -bounds.top);
            matrix.postScale(scale, scale);

            float dx = (dims - contentWidth * scale) / 2f;
            float dy = (dims - contentHeight * scale) / 2f;
            matrix.postTranslate(dx, dy);

            Path scaledPath = new Path();
            path.transform(matrix, scaledPath);

            Paint scaledPaint = new Paint(paint);
            float desiredStrokeWidth = dims * 0.08f; //Default 0.07f
            scaledPaint.setStrokeWidth(desiredStrokeWidth);
            scaledPaint.setAntiAlias(false);

            finalCanvas.drawPath(scaledPath, scaledPaint);

            return finalBitmap;
        }
    }

}