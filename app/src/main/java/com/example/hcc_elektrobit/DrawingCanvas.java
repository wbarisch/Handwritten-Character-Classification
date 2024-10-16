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

public class DrawingCanvas extends View {

    Paint paint;
    Path path;

    Bitmap bitmap;

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
    protected void onDraw(Canvas canvas){
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

    // clear the current drawing
    public void clearCanvas(){

        path.reset();
        invalidate();

    }

    // get the Bitmap
    public Bitmap getBitmap(){
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true);


        return scaledBitmap;


    }

}
