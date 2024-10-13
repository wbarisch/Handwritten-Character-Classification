package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
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

        model = new CNNonnxModel();

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

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Display the dimensions in the recognizedCharTextView
        runOnUiThread(() -> {
            // Display the dimensions in the recognizedCharTextView
            String dimensionsText = "Bitmap Dimensions: " + width + "x" + height;
            recognizedCharTextView.setText(dimensionsText);

            //Display the image for testing
            bitmapDisplay.setImageBitmap(bitmap);

            model.classify(bitmap);
        });

        drawingCanvas.clearCanvas();

    }

}