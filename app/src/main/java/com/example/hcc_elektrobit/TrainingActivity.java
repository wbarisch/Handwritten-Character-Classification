package com.example.hcc_elektrobit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {
    private static final int REVIEW_IMAGES_REQUEST = 1;

    private Bitmap bitmap;
    private CanvasTimer canvasTimer;
    private CNNonnxModel model;
    private DialogManager dialogManager;
    
    private DrawingCanvas drawingCanvas;
    private ImageSavingManager imageSavingManager;
    private ImageView trainingBitmapDisplay;
    private boolean timerStarted = false;

    private ImageButton plusButton;
    private ImageButton leaveButton;
    private View chatboxContainer;
    private Button exitButton;
    private Button okButton;
    private Button cancelButton;
    private Button reviewButton;
    private EditText characterIdInput;

    private CharacterMapping characterMapping;
    private List<Bitmap> bitmapsToSave;
    private String selectedCharacter = "";
    private int imageCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        dialogManager = new DialogManager(this);

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
        reviewButton = findViewById(R.id.review_button);
        cancelButton = findViewById(R.id.cancel_button);
        exitButton = findViewById(R.id.exit_button);
        characterIdInput = findViewById(R.id.character_id_input);


        chatboxContainer.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);

        exitButton.setOnClickListener(v -> {
            dialogManager.showExitTrainingModeDialog(this::finish);
        });

        exitButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Training Mode")
                    .setMessage("Do you want to leave training mode?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
        reviewButton.setVisibility(View.GONE);

        plusButton.setOnClickListener(v -> {
            plusButton.setVisibility(View.GONE);
            chatboxContainer.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.GONE);
        });

        cancelButton.setOnClickListener(v -> {
            chatboxContainer.setVisibility(View.GONE);
            plusButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);
        });

        okButton.setOnClickListener(v -> {
            String characterId = characterIdInput.getText().toString().trim();
            if (!characterId.isEmpty()) {
                int id = Integer.parseInt(characterId);
                selectedCharacter = characterMapping.getCharacterForId(id);
                if (!selectedCharacter.isEmpty()) {
                    chatboxContainer.setVisibility(View.GONE);

                    leaveButton.setVisibility(View.VISIBLE);
                    reviewButton.setVisibility(View.VISIBLE);
                }
            }
        });

        reviewButton.setOnClickListener(v -> {
            if (!bitmapsToSave.isEmpty() && !selectedCharacter.isEmpty()) {
                launchReviewActivity();
            } else {
                dialogManager.showNoImagesDialog();
            }
        });

        leaveButton.setOnClickListener(v -> {
            dialogManager.showLeaveTestingDialog(() -> {
                bitmapsToSave.clear();
                leaveButton.setVisibility(View.GONE);
                reviewButton.setVisibility(View.GONE);
                plusButton.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.VISIBLE);
            });
        });


        drawingCanvas.setOnTouchListener((v, event) -> {
            if (timerStarted) {
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

    private void addBitmapToSaveList(Bitmap bitmap) {
        String fileName = "temp_image_" + System.currentTimeMillis();
        imageSavingManager.saveBitmapToCache(this, bitmap, fileName);
        bitmapsToSave.add(bitmap);
    }


    private void launchReviewActivity() {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putStringArrayListExtra("image_paths", getCachedImagePaths());
        intent.putExtra("selectedCharacter", selectedCharacter);
        startActivityForResult(intent, REVIEW_IMAGES_REQUEST);
    }


    private ArrayList<String> getCachedImagePaths() {
        File cacheDir = getCacheDir();
        ArrayList<String> imagePaths = new ArrayList<>();
        for (File file : cacheDir.listFiles()) {
            if (file.getName().endsWith(".bmp")) {
                imagePaths.add(file.getAbsolutePath());
            }
        }
        return imagePaths;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REVIEW_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null && "keep_selected".equals(data.getStringExtra("operation"))) {
                resetTrainingMode();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void resetTrainingMode() {
        leaveButton.setVisibility(View.GONE);
        reviewButton.setVisibility(View.GONE);
        plusButton.setVisibility(View.VISIBLE);
        exitButton.setVisibility(View.VISIBLE);
        bitmapsToSave.clear();
        selectedCharacter = "";
    }



    private void saveImagesToFolder(Context context, String character) {
        imageSavingManager.saveSelectedImages(context, bitmapsToSave, character);
        bitmapsToSave.clear();
        imageSavingManager.clearImageCache(this);
    }

    @Override
    public void onTimeout() {
        bitmap = drawingCanvas.getBitmap(28);
        float[] processedBitmapData = model.preprocessBitmap(bitmap);
        Bitmap preprocessedBitmap = createBitmapFromFloatArray(processedBitmapData, 28, 28);

        runOnUiThread(() -> {
            //Uncomment line to show preview and set visiblity of ImageView in activity_training.xml from "gone" to "visible"
            //trainingBitmapDisplay.setImageBitmap(preprocessedBitmap);
            addBitmapToSaveList(preprocessedBitmap);
            drawingCanvas.clear();
        });

        timerStarted = false;
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
