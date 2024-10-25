// The HCC app running in driving mode.

package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;

public class MainActivity extends AppCompatActivity implements TimeoutActivity {

    // UI components
    private DrawingCanvas drawingCanvas;
    private LinearLayout outputView;
    private TextView charTextView; // To show the result of character recognition

    //
    // private ImageView bitmapDisplay;
    private SMSonnxModel model;
    private Bitmap bitmap;
    private AudioPlayer audioPlayer;

    //
    CanvasTimer canvasTimer;
    boolean timerStarted = false;

    // For handling control gestures: Touch events other than those used for drawing character i.e. "SingleTap", "DoubleTap", and so on.
    private GestureDetector gestureDetector;

    // Variable to prevent drawing gestures' interference with control gestures
    private boolean isAfterControlGesture = false; // true immediately after a control has occurred.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        outputView = findViewById(R.id.output_view);
        charTextView = findViewById(R.id.char_view);

        // FOR EXPERIMENT ONLY!
        //bitmapDisplay = findViewById(R.id.bitmap_display);

        model = new SMSonnxModel(this);
        audioPlayer = new AudioPlayer(this);

        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                canvasTimer.cancel();
                timerStarted = false;
            }

            /*
            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {

                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;

            }
             */

            if(drawingCanvas.onTouchEvent(event)){

                isAfterControlGesture = false;

            }

            // !!isAfterControlGesture: ignore MotionEvent.ACTION_UP occurring at the end of a control gesture.
            if (event.getAction() == MotionEvent.ACTION_UP && !isAfterControlGesture) {

                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;

            }

            if(gestureDetector.onTouchEvent(event)){

                if(timerStarted){
                    canvasTimer.cancel();
                    timerStarted = false;
                }

            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }

            return true;

        });

        // GestureDetector for handling SingleTap, DoubleTap, e.t.c.
        gestureDetector = new GestureDetector(this, new HCCGestureListener(this));

    }

    class HCCGestureListener extends GestureDetector.SimpleOnGestureListener {

        Context context;

        public HCCGestureListener (Context context){

            this.context = context;

        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            undoCharacter();
            isAfterControlGesture = true;
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            toggleLetterCase();
            isAfterControlGesture = true;
            return true;
        }

        // NOTE: Further <TouchEvents> are open for application specific implementation by the customer.

    }

    // Toggle between uppercase and lowercase forms of the output letter.
    // Matters only for letters whose hand drawn uppercase and lowercase forms are indistinguishable, such as "U/u".
    private void toggleLetterCase(){

        // Currently does nothing.

        // NOTE: I will implement it when the character recognition module is ready to recognize letters.

        // FOR TESTING ONLY!
        Toast.makeText(this, "Single Tap Detected", Toast.LENGTH_SHORT).show();

    }

    // Discard the last output of recognition. To be used in case of incorrect output or mistake by the user.
    private void undoCharacter(){

        // Undo output result
        drawingCanvas.clear();
        charTextView.setText("");
        outputView.setVisibility(View.INVISIBLE);

        // NOTE: Further implementation by the customer (i.e. Elektrobit) is required here to discard the character
        // from the particular application to which the output of this character recognition app is fed into.

        // FOR TESTING ONLY!
        Toast.makeText(this, "Double Tap Detected", Toast.LENGTH_SHORT).show();

    }

    // Set menu resource for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hcc_menu, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int SelectedItemId = item.getItemId();

        if(SelectedItemId == R.id.developer_mode) {
            // Switch to developer mode.
            startActivity(new Intent(MainActivity.this, JMainActivity.class));
            return true;

        } else{

            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);

        }

    }

    // What to do on timeout
    public void onTimeout(){

        classifyCharacter();
        drawingCanvas.clear();
        timerStarted = false;

    }

    // Invoke external CharacterClassifier class method from here to start processing the drawing.
    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap(105);

        if (bitmap == null) {
            Log.e("MainActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        // TO DO:
        // - Call CharacterClassifier class
        // - To display the output character, set it to "recognizedCharTextView".

        String result = model.classify_id(bitmap);

        History history = History.getInstance();
        HistoryItem historyItem = new HistoryItem(bitmap, result);

        history.saveItem(historyItem, this);

        //bitmap = createBitmapFromFloatArray(model.preprocessBitmap(bitmap), 28, 28);
        audioPlayer.PlayAudio(String.valueOf(result));
        runOnUiThread(() -> {

            charTextView.setText(String.valueOf(result));
            outputView.setVisibility(View.VISIBLE); // Show result

            //bitmapDisplay.setImageBitmap(bitmap);

        });
    }

    /*
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
     */

}