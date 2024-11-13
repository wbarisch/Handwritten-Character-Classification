// The HCC app running in driving mode.

package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;

public class DrivingMode extends AppCompatActivity implements TimeoutActivity {

    // UI components
    private DrawingCanvas drawingCanvas;

    //private LinearLayout outputView;
    //private TextView charTextView; // To show the result of character recognition

    private EditText textEditor; // To show concatenated resulting text, cursor

    StringBuffer stringBuffer = new StringBuffer(); // For storing and manipulating the result characters

    private SMSComaparisonOnnxModel model;
    private Bitmap bitmap;
    private AudioPlayerManager audioPlayer;

    //
    Timer canvasTimer;
    boolean timerStarted = false;

    private GestureDetector gestureDetector;

    // Flag to prevent classification on control gestures
    private boolean isAfterControlGesture = false; // true immediately after a control has occurred

    // Designates input mode in use
    private int inputMode = InputMode.DEFAULT;

    private GestureDetector ModeGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        //outputView = findViewById(R.id.output_view);
        //charTextView = findViewById(R.id.char_view);
        textEditor = findViewById(R.id.text_editor);

        model = SMSComaparisonOnnxModel.getInstance();
        audioPlayer = new AudioPlayerManager(this);
        SupportSet.getInstance().updateSet();

        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                canvasTimer.cancel();
                timerStarted = false;
            }

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

        // GestureDetector for controlling input mode
        ModeGestureDetector = new GestureDetector(this, new ModeGestureListener(this));

        textEditor.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ModeGestureDetector.onTouchEvent(event);
                return true;
            }
        });

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
            addSpace();
            isAfterControlGesture = true; // Prevent classification
            return true;
        }

    }

    // Delete the last character
    private void backspace(){

        // Undo output result
        //drawingCanvas.clear();
        //charTextView.setText("");
        //outputView.setVisibility(View.INVISIBLE);

        drawingCanvas.clear();
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        textEditor.setText(stringBuffer.toString());

        showMessage("Character deleted");

    }

    private void addSpace(){

        stringBuffer.append(" ");
        textEditor.setText(stringBuffer.toString());
        showMessage("Space inserted");

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

        String result = model.classifyAndReturnPredAndSimilarityMap(bitmap, inputMode).first;

        audioPlayer.setDataSource(result);
        audioPlayer.play();

        runOnUiThread(() -> {
            //charTextView.setText(result);
            //outputView.setVisibility(View.VISIBLE);

            //textEditor.setText(result);

            if(result != null && !result.isEmpty()){

                stringBuffer.append(result);
                textEditor.setText(stringBuffer.toString());

            }

        });

    }

    class ModeGestureListener extends GestureDetector.SimpleOnGestureListener {

        Context context;

        public ModeGestureListener (Context context){
            this.context = context;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent event) {return true;}

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            inputMode = InputMode.UPPERCASE;
            showMessage("Uppercase mode");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            inputMode = InputMode.LOWERCASE;
            showMessage("Lowercase mode");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, @NonNull MotionEvent event2, float velocityX, float velocityY) {
            inputMode = InputMode.NUMBER;
            showMessage("Number mode");
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent event) {
            inputMode = InputMode.DEFAULT;
            showMessage("Default mode");
        }

    }

    private void showMessage(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

}