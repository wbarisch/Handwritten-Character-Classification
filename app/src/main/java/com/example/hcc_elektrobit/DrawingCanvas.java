package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;



public class DrawingCanvas extends View {

    private Paint paint;
    private Path path;

    public DrawingCanvas(Context context, AttributeSet attributeSet){

        super(context, attributeSet);
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        setDynamicStrokeWidth();

    }

    public void setStrokeWidth(float strokeWidth) {
        paint.setStrokeWidth(strokeWidth);
        invalidate();

    }

    private void setDynamicStrokeWidth() {
        paint.setStrokeWidth(30f);
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
    protected void onDraw(@NonNull Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawPath(path, paint);
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
    public Bitmap getBitmap(int dims) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        this.draw(canvas);
        return BitmapUtils.centerAndResizeBitmap(bitmap, dims);
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        this.draw(canvas);
        return bitmap;
    }


}