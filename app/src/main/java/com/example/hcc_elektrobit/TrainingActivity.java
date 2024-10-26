package com.example.hcc_elektrobit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {
    private static final int REVIEW_IMAGES_REQUEST = 1;

    private int bitmapSize = 28;

    private Bitmap bitmap;
    private CanvasTimer canvasTimer;
    private DialogManager dialogManager;
    
    private DrawingCanvas drawingCanvas;
    private ImageSavingManager imageSavingManager;
    private boolean timerStarted = false;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        dialogManager = new DialogManager(this);

        characterMapping = new CharacterMapping();
        bitmapsToSave = new ArrayList<>();

        drawingCanvas = findViewById(R.id.fullscreen_canvas);
        imageSavingManager = new ImageSavingManager(null);

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

        cancelButton.setOnClickListener(v -> {
            dialogManager.showExitTrainingModeDialog(this::finish);
        });

        okButton.setOnClickListener(v -> {
            String characterId = characterIdInput.getText().toString().trim();
            if (!characterId.isEmpty()) {
                int id = Integer.parseInt(characterId);
                selectedCharacter = characterMapping.getCharacterForId(id);
                if (!selectedCharacter.isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(characterIdInput.getWindowToken(), 0);
                    }
                    chatboxContainer.setVisibility(View.GONE);
                    exitButton.setVisibility(View.GONE);
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
                chatboxContainer.setVisibility(View.VISIBLE);
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
        Bitmap invertedBitmap = invertBitmapColors(bitmap);
        String fileName = "temp_image_" + System.currentTimeMillis();
        imageSavingManager.saveBitmapToCache(this, invertedBitmap, fileName);
        bitmapsToSave.add(invertedBitmap);
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
                chatboxContainer.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.VISIBLE);
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
        selectedCharacter = "";
        imageSavingManager.clearImageCache(this);
    }


    @Override
    public void onTimeout() {
        bitmap = drawingCanvas.getBitmap(bitmapSize);

        runOnUiThread(() -> {

            addBitmapToSaveList(bitmap);
            drawingCanvas.clear();
        });

        timerStarted = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.training_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_bitmap_size) {
            showBitmapSizeDialog();
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
                        bitmapSize = size;
                        Toast.makeText(this, "Bitmap size set to " + bitmapSize, Toast.LENGTH_SHORT).show();
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

    public Bitmap invertBitmapColors(Bitmap originalBitmap) {
        Bitmap invertedBitmap = Bitmap.createBitmap(
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                originalBitmap.getConfig()
        );

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        int[] pixels = new int[width * height];
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int alpha = Color.alpha(color);
            int red = 255 - Color.red(color);
            int green = 255 - Color.green(color);
            int blue = 255 - Color.blue(color);
            pixels[i] = Color.argb(alpha, red, green, blue);
        }

        invertedBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return invertedBitmap;
    }

    /*TODO:
    Various options: size of the bitmap and id of the saved image. Introduce sequence. 1-9 for number, 10-36 for alphabet, any additional characters will start at 37. When adding a new support set
    , it should automatically go on from the last used number, in our case atm 36 for z.


    images should be saved 001, 002_1 -- first image of class 2, 003, 004 and so on.. not a_1, a_2, a_3.
    also, it shouldn't be saved in folder 'a' it should be saved in folder '010_1, 010_2, 010_3'. Use character id as folder

    Add an option to change how the bitmap is saved. Right now its white character on black. Implement switch toggle for black character on white background

    Change the keep selected button functionality. Example, save as training set or save as support set. Support set is saved to elektrobit/files/supportset.

     */



}
