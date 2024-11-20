// The HCC app running in driving mode.

package com.example.hcc_elektrobit.driving_mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.example.hcc_elektrobit.model.SMSComaparison;
import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.main.MainActivity;
import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.utils.AudioPlayerManager;
import com.example.hcc_elektrobit.utils.InputMode;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;

public class DrivingMode extends AppCompatActivity implements TimeoutActivity {

    // UI components
    private DrawingCanvas drawingCanvas;
    private LinearLayout outputView;
    private TextView charTextView; // To show the result of character recognition

    private SMSComaparison model;
    private Bitmap bitmap;
    private AudioPlayerManager audioPlayer;
    private TextView modeTextView;

    //
    Timer canvasTimer;
    boolean timerStarted = false;

    private GestureDetector gestureDetector;

    // Flag to prevent classification on control gestures
    private boolean isAfterControlGesture = false; // true immediately after a control has occurred

    // Designates input mode in use
    private int inputMode = InputMode.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        outputView = findViewById(R.id.output_view);
        charTextView = findViewById(R.id.char_view);
        modeTextView = findViewById(R.id.mode_as);

        model = SMSComaparison.getInstance();
        audioPlayer = new AudioPlayerManager(this);
        SupportSet.getInstance().updateSet();

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

            // !isAfterControlGesture: ignore MotionEvent.ACTION_UP occurring at the end of a control gesture.
            if (event.getAction() == MotionEvent.ACTION_UP && !isAfterControlGesture) {
                canvasTimer = new Timer(this, 1000);
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
        public boolean onDown(MotionEvent event) {return true;}

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            backspace();
            isAfterControlGesture = true; // Prevent classification
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            toggleInputMode(); //enterSpace();
            isAfterControlGesture = true; // Prevent classification
            return true;
        }

    }

    // Toggle input modes
    private void toggleInputMode(){

        String message;

        switch (inputMode){

            case InputMode.DEFAULT:
                inputMode = InputMode.UPPERCASE;
                message = "Uppercase mode";
                break;
            case InputMode.UPPERCASE:
                inputMode = InputMode.LOWERCASE;
                message = "Lowercase mode";
                break;
            case InputMode.LOWERCASE:
                inputMode = InputMode.NUMBER;
                message = "Number mode";
                break;
            default:
                inputMode = InputMode.DEFAULT;
                message = "Default mode";
        }


        runOnUiThread(() -> {
            modeTextView.setText(message);
        });

    }

    // Delete the last character
    private void backspace(){

        // Undo output result
        drawingCanvas.clear();
        charTextView.setText("");
        outputView.setVisibility(View.INVISIBLE);

        Toast.makeText(this, "Character Deleted", Toast.LENGTH_SHORT).show();

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
            startActivity(new Intent(DrivingMode.this, MainActivity.class));
            return true;

        } else{

            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);

        }

    }

    public void onTimeout(){

        if (!isAfterControlGesture) {
            classifyCharacter();
        }
        drawingCanvas.clear();
        timerStarted = false;
        isAfterControlGesture = false;

    }

    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap(105, true, 3f);

        if (bitmap == null) {
            Log.e("DrivingMode", "Bitmap is null in classifyCharacter");
            return;
        }

        String result = String.valueOf(model.classifyAndReturnPredAndSimilarityMap(bitmap, inputMode).first.charAt(0));

        audioPlayer.setDataSource(result);
        audioPlayer.play();

        runOnUiThread(() -> {
            charTextView.setText(result);
            outputView.setVisibility(View.VISIBLE);
        });

    }

}