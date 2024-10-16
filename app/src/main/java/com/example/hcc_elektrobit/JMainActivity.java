package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class JMainActivity extends AppCompatActivity implements TimeoutActivity {

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private CNNonnxModel model;
    private Bitmap bitmap;

    CanvasTimer canvasTimer;
    boolean timerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmain);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        Button shareButton = findViewById(R.id.share_button);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(bitmap);
            }
        });

        model = new CNNonnxModel(this);

        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                canvasTimer.cancel();
                timerStarted = false;
            }

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {

                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;

            }

            return true;

        });

    }

    // What to do on timeout
    public void onTimeout(){

        classifyCharacter();
        drawingCanvas.clear();
        timerStarted = false;

    }

    // Invoke external CharacterClassifier class method from here to start processing the drawing.
    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap();

        // TO DO:
        // - Call CharacterClassifier class
        // - To display the output character, set it to "recognizedCharTextView".

        int result = model.classifyAndReturnDigit(bitmap);

        bitmap = createBitmapFromFloatArray(model.preprocessBitmap(bitmap), 28, 28);

        runOnUiThread(() -> {

            recognizedCharTextView.setText(String.valueOf(result));

            //Display the image for testing
            bitmapDisplay.setImageBitmap(bitmap);

        });

    }

    public Bitmap createBitmapFromFloatArray(float[] floatArray, int width, int height) {
        // Ensure that the float array length matches width * height
        if (floatArray.length != width * height) {
            throw new IllegalArgumentException("Float array length must match width * height");
        }

        // Create a bitmap with the specified width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create an array to hold the pixel colors
        int[] pixels = new int[width * height];

        // Iterate over the float array and convert each value to a grayscale color
        for (int i = 0; i < floatArray.length; i++) {
            float value = floatArray[i];  // Get the float value

            // Ensure the value is clamped between 0 and 1
            value = Math.max(0, Math.min(1, value));

            // Convert the float value to an integer between 0 and 255
            int grayscale = (int) (value * 255);

            // Create a grayscale color (same value for R, G, and B, and full alpha)
            int color = Color.argb(255, grayscale, grayscale, grayscale);

            // Set the color in the pixel array
            pixels[i] = color;
        }

        // Set the pixel data to the bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    private void shareImage(Bitmap bitmap) {
        // To be implemented
    }

}