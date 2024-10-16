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

    Paint paint;
    Path path;

    public DrawingCanvas(Context context, AttributeSet attributeSet){

        super(context, attributeSet);
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(100f);

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas){
        super.onDraw(canvas);
        canvas.drawPath(path,paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        float xPos = event.getX();
        float yPos = event.getY();

        switch(event.getAction()){

            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos,yPos);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos,yPos);
                invalidate();
                return true;

            default:
                return false;
        }

    }

    // Erase the current drawing
    public void clear(){

        path.reset();
        invalidate();

    }

    // Get bitmap of the drawing
    public Bitmap getBitmap(){

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        return Bitmap.createScaledBitmap(bitmap, 28, 28, true);
    }

}