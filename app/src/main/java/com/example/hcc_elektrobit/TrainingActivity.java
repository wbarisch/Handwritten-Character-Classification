package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {
    private Bitmap bitmap;
    private CanvasTimer canvasTimer;
    private CNNonnxModel model;
    private DrawingCanvas drawingCanvas;
    private ImageSavingManager imageSavingManager;
    private ImageView trainingBitmapDisplay;
    private boolean timerStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        drawingCanvas = findViewById(R.id.fullscreen_canvas);
        trainingBitmapDisplay = findViewById(R.id.training_bitmap_display);
        imageSavingManager = new ImageSavingManager(null);
        model = new CNNonnxModel(this);

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(v -> finish());

        drawingCanvas.setOnTouchListener((v, event) -> {
            if (timerStarted) {

                canvasTimer.cancel();
                timerStarted = false;
            }

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                bitmap = drawingCanvas.getBitmap(28);
                float[] processedBitmapData = model.preprocessBitmap(bitmap);
                Bitmap preprocessedBitmap = createBitmapFromFloatArray(processedBitmapData, 28, 28);
                trainingBitmapDisplay.setImageBitmap(preprocessedBitmap);

                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }

            return true;
        });
    }

    @Override
    public void onTimeout() {
        bitmap = drawingCanvas.getBitmap(28);
        float[] processedBitmapData = model.preprocessBitmap(bitmap);
        Bitmap preprocessedBitmap = createBitmapFromFloatArray(processedBitmapData, 28, 28);
        drawingCanvas.clear();
        timerStarted = false;
        imageSavingManager.saveImageToDevice(this, preprocessedBitmap);
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
            int color = android.graphics.Color.argb(255, grayscale, grayscale, grayscale);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
