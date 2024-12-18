package com.example.hcc_elektrobit.keyboard_mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.main.MainActivity;
import com.example.hcc_elektrobit.model.SMSComaparison;
import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.utils.AudioPlayerManager;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;

public class KeyboardModeActivity extends AppCompatActivity implements TimeoutActivity, SpellCheckerSession.SpellCheckerSessionListener {

    // UI components
    private DrawingCanvas drawingCanvas;
    private LinearLayout outputView;
    private TextView charTextView;
    private EditText textBox; // New cumulative output text box

    private SMSComaparison model;
    private Bitmap bitmap;
    private AudioPlayerManager audioPlayer;

    private Timer canvasTimer;
    private boolean timerStarted = false;
    private boolean isAfterControlGesture = false; // Flag to prevent classification on control gestures

    private GestureDetector gestureDetector;
    private SpellCheckerSession spellCheckerSession;

    private boolean isCanvasLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboardmode);

        // Initialize views and model
        drawingCanvas = findViewById(R.id.drawing_canvas);
        outputView = findViewById(R.id.output_view);
        charTextView = findViewById(R.id.char_view);
        textBox = findViewById(R.id.text_box); // Initialize cumulative text box

        model = SMSComaparison.getInstance();
        audioPlayer = new AudioPlayerManager(this);
        SupportSet.getInstance().updateSet();

        // Set up the Android spell checker session
        TextServicesManager textServicesManager = (TextServicesManager) getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
        if (textServicesManager != null) {
            spellCheckerSession = textServicesManager.newSpellCheckerSession(null, null, this, true);
        }

        // Set up touch listener for drawingCanvas
        drawingCanvas.setOnTouchListener((v, event) -> {

            if (isCanvasLocked) {
                return true;
            }

            if (timerStarted) {
                canvasTimer.cancel();
                timerStarted = false;
            }

            if (drawingCanvas.onTouchEvent(event)) {
                isAfterControlGesture = false;
            }

            if (event.getAction() == MotionEvent.ACTION_UP && !isAfterControlGesture) {
                canvasTimer = new Timer(this,500);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }

            if (gestureDetector.onTouchEvent(event)) {
                if (timerStarted) {
                    canvasTimer.cancel();
                    timerStarted = false;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Set up GestureDetector for single and double taps
        gestureDetector = new GestureDetector(this, new HCCGestureListener(this));
    }

    class HCCGestureListener extends GestureDetector.SimpleOnGestureListener {

        Context context;

        public HCCGestureListener(Context context) {
            this.context = context;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (isCanvasLocked) {
                // If canvas is already locked, ignore additional double-taps
                return true;
            }

            handleBackspace();
            isAfterControlGesture = true;  // Prevent classification

            // Lock the canvas to prevent further processing
            lockCanvasTemporarily();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            handleSpace();
            isAfterControlGesture = true;  // Prevent classification
            return true;
        }
    }

    @Override
    public void onTimeout() {
        if (!isAfterControlGesture && !isCanvasLocked) {
            classifyCharacter();
        }
        drawingCanvas.clear();
        timerStarted = false;
        isAfterControlGesture = false;
    }

    private void lockCanvasTemporarily() {
        isCanvasLocked = true;

        disableAutocorrectTemporarily();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isCanvasLocked = false;
            isAfterControlGesture = false;
            enableAutocorrect();
        }, 100);
    }

    private void classifyCharacter() {
        bitmap = drawingCanvas.getBitmap(105, true, 3f);

        if (bitmap == null) {
            Log.e("KeyboardModeActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        String result = String.valueOf(model.classifyAndReturnPredAndSimilarityMap(bitmap).first.charAt(0));

        audioPlayer.setDataSource(result);
        audioPlayer.play();

        runOnUiThread(() -> {
            charTextView.setText(result);
            addText(result); // Add recognized character to cumulative text box
            outputView.setVisibility(View.VISIBLE);
        });
    }

    private void addText(String character) {
        String currentText = textBox.getText().toString();
        Log.e("kbac", currentText);
        if((!currentText.isEmpty())&& !(currentText.endsWith(" "))){
            character = character.toLowerCase();
        }
        textBox.setText(currentText + character);
    }

    private void handleSpace() {
        String currentText = textBox.getText().toString();
        if (!currentText.isEmpty() && !currentText.endsWith(" ")) {
            textBox.setText(currentText + " ");
            spellCheckLastWord();
        }
    }

    private void handleBackspace() {
        String currentText = textBox.getText().toString();
        if (!currentText.isEmpty()) {
            textBox.setText(currentText.substring(0, currentText.length() - 1));
        }
    }

    private void spellCheckLastWord() {
        String currentText = textBox.getText().toString().trim();
        if (currentText.isEmpty()) return;

        String[] words = currentText.split(" ");
        String lastWord = words[words.length - 1];

        if (spellCheckerSession != null) {
            spellCheckerSession.getSuggestions(new TextInfo(lastWord), 5);
        }
    }

    private void disableAutocorrectTemporarily() {
        runOnUiThread(() -> {
            textBox.setInputType(textBox.getInputType() & ~InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        });
    }

    private void enableAutocorrect() {
        runOnUiThread(() -> {
            textBox.setInputType(textBox.getInputType() | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        });
    }

    // Implementing the spell checker session listener
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        if (results == null || results.length == 0) return;
        SuggestionsInfo suggestionsInfo = results[0];
        if (suggestionsInfo.getSuggestionsCount() > 0) {
            StringBuilder suggestedWord = new StringBuilder(suggestionsInfo.getSuggestionAt(0));

            runOnUiThread(() -> {
                String currentText = textBox.getText().toString().trim();
                String[] words = currentText.split(" ");
                words[words.length - 1] = suggestedWord.toString();  // Replace last word with suggestion

                textBox.setText(TextUtils.join(" ", words) + " ");
            });
        }
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hcc_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int SelectedItemId = item.getItemId();

        if (SelectedItemId == R.id.developer_mode) {
            startActivity(new Intent(KeyboardModeActivity.this, MainActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (spellCheckerSession != null) {
            spellCheckerSession.close();
        }
    }
}