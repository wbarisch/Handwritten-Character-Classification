package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {
    private Bitmap bitmap;
    private CanvasTimer canvasTimer;
    private CNNonnxModel model;
    private DrawingCanvas drawingCanvas;
    private ImageSavingManager imageSavingManager;
    private ImageView trainingBitmapDisplay;
    private boolean timerStarted = false;

    private ImageButton plusButton;
    private ImageButton leaveButton;
    private View chatboxContainer;
    private Button okButton;
    private Button cancelButton;
    private EditText characterIdInput;

    private CharacterMapping characterMapping;
    private List<Bitmap> bitmapsToSave;
    private String selectedCharacter = "";
    private int imageCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        characterMapping = new CharacterMapping();
        bitmapsToSave = new ArrayList<>();

        drawingCanvas = findViewById(R.id.fullscreen_canvas);
        trainingBitmapDisplay = findViewById(R.id.training_bitmap_display);
        imageSavingManager = new ImageSavingManager(null);
        model = new CNNonnxModel(this);

        plusButton = findViewById(R.id.plus_button);
        leaveButton = findViewById(R.id.leave_button);
        chatboxContainer = findViewById(R.id.chatbox_container);
        okButton = findViewById(R.id.ok_button);
        cancelButton = findViewById(R.id.cancel_button);
        characterIdInput = findViewById(R.id.character_id_input);

        chatboxContainer.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(v -> finish());

        plusButton.setOnClickListener(v -> {
            plusButton.setVisibility(View.GONE);
            chatboxContainer.setVisibility(View.VISIBLE);
        });


        cancelButton.setOnClickListener(v -> {
            chatboxContainer.setVisibility(View.GONE);
            plusButton.setVisibility(View.VISIBLE);
        });

        okButton.setOnClickListener(v -> {
            String characterId = characterIdInput.getText().toString().trim();
            if (!characterId.isEmpty()) {
                int id = Integer.parseInt(characterId);
                selectedCharacter = characterMapping.getCharacterForId(id); // Get character for ID
                if (!selectedCharacter.isEmpty()) {
                    chatboxContainer.setVisibility(View.GONE);
                    leaveButton.setVisibility(View.VISIBLE);
                }
            }
        });


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

                addBitmapToSaveList(preprocessedBitmap);

                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }

            return true;
        });

        leaveButton.setOnClickListener(v -> {
            if (!bitmapsToSave.isEmpty() && !selectedCharacter.isEmpty()) {
                saveImagesToFolder(this, selectedCharacter); // Save bitmaps in the selected character folder
            }
            leaveButton.setVisibility(View.GONE);
            plusButton.setVisibility(View.VISIBLE);
        });
    }

    private void addBitmapToSaveList(Bitmap bitmap) {
        bitmapsToSave.add(bitmap);
    }

    private void saveImagesToFolder(Context context, String character) {
        for (Bitmap bitmap : bitmapsToSave) {
            String filename = character + "_" + imageCounter + ".bmp"; // e.g., a_1.bmp, a_2.bmp
            imageSavingManager.saveImageToCharacterFolder(context, bitmap, character, filename);
            imageCounter++;
        }
        bitmapsToSave.clear(); // Clear the list after saving
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
