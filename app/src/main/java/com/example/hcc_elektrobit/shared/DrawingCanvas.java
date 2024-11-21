package com.example.hcc_elektrobit.shared;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;

import com.example.hcc_elektrobit.utils.BitmapUtils;

public class DrawingCanvas extends View {
    private Paint paint;
    private Path path;



    private float currentStrokeWidth = 50f;

    public DrawingCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(false);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        setDynamicStrokeWidth();
    }

    public void setStrokeWidth(float strokeWidth) {
        paint.setStrokeWidth(strokeWidth);
        currentStrokeWidth = strokeWidth;
        invalidate();
    }

    private void setDynamicStrokeWidth() {
        paint.setStrokeWidth(currentStrokeWidth);
        invalidate();
    }

    public Paint getPaint() {
        return paint;
    }

    public void setAntiAlias(boolean antiAlias) {
        paint.setAntiAlias(antiAlias);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos, yPos);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos, yPos);
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    public Path getDrawnPath() {
        return new Path(path);
    }
    public Bitmap getBitmap(int desiredSize, boolean useFixedSize, float outputStrokeWidth) {
        if (getWidth() > 0 && getHeight() > 0) {
            if (useFixedSize) {
                Path drawnPath = getDrawnPath();
                return BitmapUtils.drawPathToBitmap(drawnPath, getWidth(), getHeight(), desiredSize, outputStrokeWidth, paint.isAntiAlias());
            } else {
                Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                this.draw(canvas);
                return BitmapUtils.centerAndResizeBitmap(bitmap, desiredSize, paint.isAntiAlias());
            }
        } else {
            throw new IllegalArgumentException("Width and height must be > 0");
        }
    }

    public Bitmap getBitmap(int desiredSize, boolean useFixedSize) {
        return getBitmap(desiredSize, useFixedSize, 3f);
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        this.draw(canvas);
        return bitmap;
    }

    public int getCurrentStrokeWidth() {
        return (int) currentStrokeWidth;
    }
}