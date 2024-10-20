package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;

public class SiameseTesterActivity extends AppCompatActivity implements TimeoutActivity {

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private ImageView bitmapDisplay2;
    private SMSonnxModel model;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private AudioPlayer audioPlayer;
    private CanvasTimer canvasTimer;
    private boolean timerStarted = false;
    int bitmapState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siamesetester);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        bitmapDisplay2 = findViewById(R.id.bitmap_display2);



        model = new SMSonnxModel(this);




        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                canvasTimer.cancel();
                timerStarted = false;
            }

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                bitmap = drawingCanvas.getBitmap(105);
                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }

            return true;
        });
    }


    public void onTimeout(){

        findSimilarity();
        drawingCanvas.clear();
        timerStarted = false;

    }

    private void findSimilarity(){

       if(bitmapState == 0) {

            bitmap = drawingCanvas.getBitmap(105);
            runOnUiThread(() -> {
                bitmapDisplay2.setImageDrawable(null);
                bitmapDisplay.setImageBitmap(createBitmapFromFloatArray(model.preprocessBitmap(bitmap),105,105));
                recognizedCharTextView.setText("_");


            });
            bitmapState = 1;
        } else {
            bitmap2 = drawingCanvas.getBitmap(105);

            float similarity = model.classify_similarity(bitmap,bitmap2);

            runOnUiThread(() -> {

                bitmapDisplay2.setImageBitmap(createBitmapFromFloatArray(model.preprocessBitmap(bitmap2),105,105));
                recognizedCharTextView.setText(String.valueOf(similarity));

            });
            bitmapState = 0;

        }




    }

    public Bitmap createBitmapFromFloatArray(float[] floatArray, int width, int height) {

        if (floatArray.length != width * height) {
            throw new IllegalArgumentException("Float array length must match width * height");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];

        for (int i = 0; i < floatArray.length; i++) {

            float value = floatArray[i];
            value = Math.max(0, Math.min(1, value));
            int grayscale = (int) (value * 255);
            int color = Color.argb(255, grayscale, grayscale, grayscale);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}