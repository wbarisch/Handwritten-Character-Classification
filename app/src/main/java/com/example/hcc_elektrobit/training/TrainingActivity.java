package com.example.hcc_elektrobit.training;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;
import com.example.hcc_elektrobit.history.JHistoryActivity;
import com.example.hcc_elektrobit.utils.CharacterMapping;
import com.example.hcc_elektrobit.utils.DialogManager;
import com.example.hcc_elektrobit.utils.ImageSavingManager;

import org.opencv.core.Core;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {

    private static final String TAG = "TrainingViewModel";
    private static final int REVIEW_IMAGES_REQUEST = 1;

    private Bitmap bitmap;
    private Timer canvasTimer;
    private DialogManager dialogManager;
    private ImageSavingManager imageSavingManager;
    private DrawingCanvas drawingCanvas;
    private boolean timerStarted = false;
    private ImageButton leaveButton;
    private View chatboxContainer;
    private Button exitButton;
    private Button okButton;
    private Button cancelButton;
    private Button reviewButton;
    private EditText characterIdInput;
    private List<Bitmap> bitmapsToSave;
    private TrainingActivityViewModel viewModel;

    static {
        try {
            System.loadLibrary("opencv_java4"); // Adjust if needed
            Log.d("OpenCV", "OpenCV library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e("OpenCV", "Failed to load OpenCV library", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        imageSavingManager = new ImageSavingManager(null, new CharacterMapping());
        bitmapsToSave = new ArrayList<>();
        viewModel = new ViewModelProvider(this).get(TrainingActivityViewModel.class);
        dialogManager = new DialogManager(this);
        drawingCanvas = findViewById(R.id.fullscreen_canvas);
        leaveButton = findViewById(R.id.leave_button);
        chatboxContainer = findViewById(R.id.chatbox_container);
        okButton = findViewById(R.id.ok_button);
        reviewButton = findViewById(R.id.review_button);
        cancelButton = findViewById(R.id.cancel_button);
        exitButton = findViewById(R.id.exit_button);
        characterIdInput = findViewById(R.id.character_id_input);

        chatboxContainer.setVisibility(View.VISIBLE);
        exitButton.setVisibility(View.VISIBLE);
        leaveButton.setVisibility(View.GONE);
        reviewButton.setVisibility(View.GONE);

        setupObservers();
        setupUIListeners();

        Log.d(TAG, "OpenCV Version: " + Core.VERSION);
    }

    private void addBitmapToSaveList(Bitmap bitmap) {
        String fileName = "temp_image_" + System.currentTimeMillis();
        // Assuming you have an instance of ImageSavingManager in the Activity
        if (imageSavingManager == null) {
            imageSavingManager = new ImageSavingManager(null, new CharacterMapping());
        }
        imageSavingManager.saveBitmapToCache(this, bitmap, fileName);
        bitmapsToSave.add(bitmap);
    }

    private void setupObservers() {
        viewModel.getMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSelectedCharacterLiveData().observe(this, selectedCharacter -> {
            if (!selectedCharacter.isEmpty()) {
                hideKeyboard();
                chatboxContainer.setVisibility(View.GONE);
                exitButton.setVisibility(View.GONE);
                leaveButton.setVisibility(View.VISIBLE);
                reviewButton.setVisibility(View.VISIBLE);
            } else {
                chatboxContainer.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.VISIBLE);
                leaveButton.setVisibility(View.GONE);
                reviewButton.setVisibility(View.GONE);
            }
        });

        viewModel.getLaunchReviewActivityEvent().observe(this, event -> {
            if (event != null) {
                // Ensures the event is handled only once
                if (event.getContentIfNotHandled() != null) {
                    launchReviewActivity();
                }
            }
        });

        viewModel.getNoImagesDialogEvent().observe(this, event -> {
            if (event != null) {
                if (event.getContentIfNotHandled() != null) {
                    dialogManager.showNoImagesDialog();
                }
            }
        });
    }

    private void setupUIListeners() {
        exitButton.setOnClickListener(v -> {
            dialogManager.showExitTrainingModeDialog(this::finish);
        });

        cancelButton.setOnClickListener(v -> {
            dialogManager.showExitTrainingModeDialog(this::finish);
        });

        okButton.setOnClickListener(v -> {
            String characterIdStr = characterIdInput.getText().toString().trim();
            viewModel.setSelectedCharacterId(characterIdStr);
        });

        reviewButton.setOnClickListener(v -> {
            if (!bitmapsToSave.isEmpty() && !viewModel.getSelectedCharacter().isEmpty()) {
                launchReviewActivity();
            } else {
                dialogManager.showNoImagesDialog();
            }
        });

        leaveButton.setOnClickListener(v -> {
            dialogManager.showLeaveTestingDialog(() -> {
                resetTrainingMode();
                leaveButton.setVisibility(View.GONE);
                reviewButton.setVisibility(View.GONE);
                chatboxContainer.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.VISIBLE);
            });
        });

        drawingCanvas.setOnTouchListener((v, event) -> {
            if (chatboxContainer.getVisibility() == View.VISIBLE) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(this, "Please enter a Character ID before drawing.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            drawingCanvas.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (canvasTimer != null) {
                        canvasTimer.cancel();
                        timerStarted = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (canvasTimer != null) {
                        canvasTimer.cancel(); // Cancel any existing timer
                    }

                    // Create a new Timer instance and start it in a separate thread
                    canvasTimer = new Timer(this, 1000);
                    new Thread(canvasTimer).start();
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    @Override
    public void onTimeout() {
        Bitmap bitmap = drawingCanvas.getBitmap();
        runOnUiThread(() -> {
            List<Bitmap> extractedBitmaps = ImageProcessingHelper.processAndSegmentWord(
                    bitmap, viewModel.isSaveAsWhiteCharacterOnBlack(), viewModel.getBitmapSize()
            );
            if (extractedBitmaps.isEmpty()) {
                Toast.makeText(this, "No characters found", Toast.LENGTH_SHORT).show();
            } else {
                for (Bitmap centeredBitmap : extractedBitmaps) {
                    addBitmapToSaveList(centeredBitmap);
                }
                Toast.makeText(this, "Extracted " + extractedBitmaps.size() + " characters", Toast.LENGTH_SHORT).show();
            }
            drawingCanvas.clear();
        });
        timerStarted = false;
    }

    private void launchReviewActivity() {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putStringArrayListExtra("image_paths", getCachedImagePaths());
        Integer selectedCharacterId = viewModel.getSelectedCharacterIdLiveData().getValue();
        if (selectedCharacterId != null) {
            intent.putExtra("selectedCharacterId", selectedCharacterId);
        }
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
        exitButton.setVisibility(View.VISIBLE);
        chatboxContainer.setVisibility(View.VISIBLE);
        bitmapsToSave.clear();
        if (imageSavingManager != null) {
            imageSavingManager.clearImageCache(this);
        }
        viewModel.resetSelectedCharacter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.training_activity_menu, menu);
        MenuItem toggleAntiAliasItem = menu.findItem(R.id.action_toggle_antialias);
        MenuItem selectStrokeWidthItem = menu.findItem(R.id.action_select_stroke_width);
        if (drawingCanvas != null) {
            if (toggleAntiAliasItem != null) {
                toggleAntiAliasItem.setChecked(drawingCanvas.getPaint().isAntiAlias());
            }
            if (selectStrokeWidthItem != null) {
                selectStrokeWidthItem.setEnabled(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuButton) {
            Intent intent = new Intent(TrainingActivity.this, JHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_bitmap_size) {
            showBitmapSizeDialog();
            return true;
        } else if (id == R.id.menu_toggle_bitmap_mode) {
            dialogManager.showToggleBitmapModeDialog(viewModel.isSaveAsWhiteCharacterOnBlack(), newMode -> {
                viewModel.setSaveAsWhiteCharacterOnBlack(newMode);
                String mode = newMode ? "White Character on Black" : "Black Character on White";
                Toast.makeText(this, "Bitmap mode set to: " + mode, Toast.LENGTH_SHORT).show();
            });
            return true;
        } else if (id == R.id.action_toggle_antialias) {
            item.setChecked(!item.isChecked());
            if (drawingCanvas != null) {
                drawingCanvas.setAntiAlias(item.isChecked());
            }
            Log.d(TAG, "Anti-Alias set to: " + item.isChecked());
            return true;
        } else if (id == R.id.action_select_stroke_width) {
            dialogManager.showStrokeWidthInputDialog(drawingCanvas);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBitmapSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Bitmap Image Size");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter bitmap size (e.g., 28)");
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String inputText = input.getText().toString().trim();
            if (!inputText.isEmpty()) {
                try {
                    int size = Integer.parseInt(inputText);
                    if (size > 0) {
                        viewModel.setBitmapSize(size);
                        Toast.makeText(this, "Bitmap size set to " + size, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a positive number.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Input cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && characterIdInput != null) {
            imm.hideSoftInputFromWindow(characterIdInput.getWindowToken(), 0);
        }
    }
}