package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class JMainActivity extends AppCompatActivity {

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private CNNonnxModel model;

    boolean noActivity; // :true if no drawing activity on the canvas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmain);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);

        model = new CNNonnxModel(this);

        noActivity = true; // initialize as inactive

        drawingCanvas.setOnTouchListener((v, event) -> {


            noActivity = false;
            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {

                setTimeOut(); // invoke "classifyCharacter after 1 second of inactivity"
            }

            return true;

        });

    }

    // Invoke the method classifyCharacter() after 1 second of inactivity"
    private void setTimeOut(){

        noActivity = true;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (noActivity){
                    classifyCharacter();
                }

            }
        };

        new Thread(runnable).start();

    }

    // Invoke external CharacterClassifier class from here to start processing the drawing.
    private void classifyCharacter(){

        Bitmap bitmap = drawingCanvas.getBitmap(); // ! The return value invalid currently

        // TO DO:
        // - Call CharacterClassifier class
        // - To display the output character, set it to "recognizedCharTextView".

        int result = model.classifyAndReturnDigit(bitmap);

        runOnUiThread(() -> {

            recognizedCharTextView.setText(String.valueOf(result));

            //Display the image for testing
            bitmapDisplay.setImageBitmap(createBitmapFromFloatArray(CNNonnxModel.preprocessBitmap(bitmap), 28, 28));


        });

        drawingCanvas.clearCanvas();

    }

    public static Bitmap createBitmapFromFloatArray(float[] floatArray, int width, int height) {
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

}