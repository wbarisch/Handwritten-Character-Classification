// The HCC app running in driving/user mode.

package com.example.hcc_elektrobit.driving_mode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;

import com.example.hcc_elektrobit.model.SMSComaparison;
import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.utils.AudioPlayerManager;
import com.example.hcc_elektrobit.utils.InputMode;

import java.util.TimerTask;

public class DrivingMode extends AppCompatActivity {

    // UI components
    private DrawingCanvas drawingCanvas;

    private EditText textEditor; // To show concatenated result text

    StringBuffer stringBuffer = new StringBuffer(); // For storing and manipulating the result characters

    private SMSComaparison model;
    private AudioPlayerManager audioPlayer;

    java.util.Timer timer; // Timer for synchronization of drawing and recognition.

    boolean timerStarted = false;

    private GestureDetector gestureDetector;

    // Flag to prevent classification on control gestures
    private boolean isAfterControlGesture = false; // true immediately after a control has occurred

    // Designates input mode in use
    private int inputMode = InputMode.DEFAULT;

    private GestureDetector ModeGestureDetector;

    // Input mode indicators
    private TextView uppercaseSign, lowercaseSign, numberSign;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        textEditor = findViewById(R.id.text_editor);

        uppercaseSign = findViewById(R.id.uppercase_sign);
        lowercaseSign = findViewById(R.id.lowercase_sign);
        numberSign = findViewById(R.id.number_sign);

        model = SMSComaparison.getInstance();
        audioPlayer = new AudioPlayerManager(this);
        SupportSet.getInstance().updateSet();

        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                timer.cancel();
                timerStarted = false;
            }

            if(drawingCanvas.onTouchEvent(event)){
                isAfterControlGesture = false;
            }

            // !isAfterControlGesture: ignore MotionEvent.ACTION_UP occurring at the end of a control gesture.
            if (event.getAction() == MotionEvent.ACTION_UP && !isAfterControlGesture) {

                timer = new java.util.Timer(); // Create a timer

                // Create a TimerTask
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        onTimeout();
                    }
                };
                timer.schedule(task, 1000);
                timerStarted = true;
            }

            if(gestureDetector.onTouchEvent(event)){
                if(timerStarted){
                    timer.cancel();
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

        // GestureDetector for controlling input mode
        ModeGestureDetector = new GestureDetector(this, new ModeGestureListener(this));

        textEditor.setOnTouchListener((v, event) -> {
            ModeGestureDetector.onTouchEvent(event);
            return true;
        });

    }

    // GestureListener for handling touch gestures on the drawing canvas
    class HCCGestureListener extends GestureDetector.SimpleOnGestureListener {

        Context context;

        public HCCGestureListener (Context context){
            this.context = context;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent event) {return true;}

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            backspace();
            isAfterControlGesture = true; // Prevent classification
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            addSpace();
            isAfterControlGesture = true; // Prevent classification
            return true;
        }

    }

    // Delete the last character
    private void backspace(){
        drawingCanvas.clear();
        if(stringBuffer.length()>0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            textEditor.setText(stringBuffer.toString());
            showMessage("Character deleted");
        }
    }

    // Insert space
    private void addSpace(){
        stringBuffer.append(" ");
        textEditor.setText(stringBuffer.toString());
        showMessage("Space inserted");
    }

    // Task to do when the timer times out
    public void onTimeout(){

        if (!isAfterControlGesture) {
            classifyCharacter();
        }
        drawingCanvas.clear();
        timerStarted = false;
        isAfterControlGesture = false;

    }

    // Classify the drawn image as a character
    private void classifyCharacter(){

        Bitmap bitmap = drawingCanvas.getBitmap(105, true, 3f);

        if (bitmap == null) {
            Log.e("DrivingMode", "Bitmap is null in classifyCharacter");
            return;
        }

        String result = String.valueOf(model.classifyAndReturnPredAndSimilarityMap(bitmap, inputMode).first.charAt(0));

        audioPlayer.setDataSource(result);
        audioPlayer.play();

        runOnUiThread(() -> {

            if(!result.isEmpty()){

                stringBuffer.append(result);
                textEditor.setText(stringBuffer.toString());

            }

        });

    }

    // GestureListener for switching input modes with touch gestures
    class ModeGestureListener extends GestureDetector.SimpleOnGestureListener {

        Context context;

        public ModeGestureListener (Context context){
            this.context = context;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent event) {return true;}

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) { // Set input mode to uppercase letters
            inputMode = InputMode.UPPERCASE;
            uppercaseSign.setBackgroundResource(R.drawable.selected_sign);
            lowercaseSign.setBackgroundResource(R.drawable.not_selected_sign);
            numberSign.setBackgroundResource(R.drawable.not_selected_sign);
            showMessage("Uppercase mode");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) { // Set input mode to lowercase letters
            inputMode = InputMode.LOWERCASE;
            uppercaseSign.setBackgroundResource(R.drawable.not_selected_sign);
            lowercaseSign.setBackgroundResource(R.drawable.selected_sign);
            numberSign.setBackgroundResource(R.drawable.not_selected_sign);
            showMessage("Lowercase mode");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, @NonNull MotionEvent event2, float velocityX, float velocityY) { // Set input mode to numerical digits
            inputMode = InputMode.NUMBER;
            uppercaseSign.setBackgroundResource(R.drawable.not_selected_sign);
            lowercaseSign.setBackgroundResource(R.drawable.not_selected_sign);
            numberSign.setBackgroundResource(R.drawable.selected_sign);
            showMessage("Number mode");
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent event) { // Set input mode to default i.e. all character types
            inputMode = InputMode.DEFAULT;
            uppercaseSign.setBackgroundResource(R.drawable.selected_sign);
            lowercaseSign.setBackgroundResource(R.drawable.selected_sign);
            numberSign.setBackgroundResource(R.drawable.selected_sign);
            showMessage("Default mode");
        }

    }

    // Display brief notification
    private void showMessage(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

}