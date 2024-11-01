package com.example.hcc_elektrobit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.graphics.Canvas;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private boolean saveAsWhiteCharacterOnBlack = true;

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
    private int selectedCharacterId = -1;


    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV via OpenCVLoader.initDebug()");
            try {
                System.loadLibrary("opencv_java4");
                Log.d("OpenCV", "OpenCV library loaded manually");
            } catch (UnsatisfiedLinkError e) {
                Log.e("OpenCV", "Failed to load OpenCV native library manually", e);
            }
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully using OpenCVLoader.initDebug()");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);


        dialogManager = new DialogManager(this);

        characterMapping = new CharacterMapping();
        bitmapsToSave = new ArrayList<>();

        drawingCanvas = findViewById(R.id.fullscreen_canvas);
        imageSavingManager = new ImageSavingManager(null, characterMapping);

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
            String characterIdStr = characterIdInput.getText().toString().trim();
            if (!characterIdStr.isEmpty()) {
                try {
                    int id = Integer.parseInt(characterIdStr);
                    selectedCharacterId = id; // Assign to the class-level variable
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
                    } else {
                        Toast.makeText(this, "Invalid character ID.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    Log.e("TrainingActivity", "Invalid number format for character ID.", e);
                }
            } else {
                Toast.makeText(this, "Character ID cannot be empty.", Toast.LENGTH_SHORT).show();
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
                        canvasTimer.stopTimer();
                        timerStarted = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (canvasTimer == null) {
                        canvasTimer = new CanvasTimer(this, 1000);
                    }
                    if (!timerStarted) {
                        canvasTimer.startTimer();
                        timerStarted = true;
                    } else {
                        canvasTimer.resetTimer();
                    }
                    break;
                default:
                    break;
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
        intent.putExtra("selectedCharacterId", selectedCharacterId);
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
        bitmap = drawingCanvas.getBitmap();
        runOnUiThread(() -> {
            processAndSegmentWord(bitmap);
            drawingCanvas.clear();
        });
        timerStarted = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.training_activity_menu, menu);

        MenuItem toggleBitmapMethodItem = menu.findItem(R.id.action_toggle_bitmap_method);
        MenuItem toggleAntiAliasItem = menu.findItem(R.id.action_toggle_antialias);
        MenuItem selectStrokeWidthItem = menu.findItem(R.id.action_select_stroke_width);

        if (drawingCanvas != null) {
            boolean useOldBitmapMethod = drawingCanvas.isUseOldBitmapMethod();
            if (toggleBitmapMethodItem != null) {
                toggleBitmapMethodItem.setChecked(useOldBitmapMethod);
            }

            if (toggleAntiAliasItem != null) {
                toggleAntiAliasItem.setChecked(!useOldBitmapMethod && drawingCanvas.getPaint().isAntiAlias());
                toggleAntiAliasItem.setEnabled(!useOldBitmapMethod);
            }

            if (selectStrokeWidthItem != null) {
                selectStrokeWidthItem.setEnabled(!useOldBitmapMethod);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_bitmap_size) {
            showBitmapSizeDialog();
            return true;
        } else if (id == R.id.menu_toggle_bitmap_mode) {
            dialogManager.showToggleBitmapModeDialog(saveAsWhiteCharacterOnBlack, newMode -> {
                saveAsWhiteCharacterOnBlack = newMode;
                String mode = saveAsWhiteCharacterOnBlack ? "White Character on Black" : "Black Character on White";
                Toast.makeText(this, "Bitmap mode set to: " + mode, Toast.LENGTH_SHORT).show();
            });
            return true;
        } else if (id == R.id.action_toggle_bitmap_method) {
            item.setChecked(!item.isChecked());

            if (drawingCanvas != null) {
                drawingCanvas.setUseOldBitmapMethod(item.isChecked());
            }

            invalidateOptionsMenu();

            Log.d("TrainingActivity", "Use Old Bitmap Method set to: " + item.isChecked());
            return true;
        } else if (id == R.id.action_toggle_antialias) {
            if (!drawingCanvas.isUseOldBitmapMethod()) {
                item.setChecked(!item.isChecked());
                if (drawingCanvas != null) {
                    drawingCanvas.setAntiAlias(item.isChecked());
                }
                Log.d("TrainingActivity", "Anti-Alias set to: " + item.isChecked());
            }
            return true;
        } else if (id == R.id.action_select_stroke_width) {
            if (!drawingCanvas.isUseOldBitmapMethod()) {
                dialogManager.showStrokeWidthInputDialog(drawingCanvas);
            }
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

    public Bitmap adjustBitmapColors(Bitmap originalBitmap) {
        Bitmap adjustedBitmap = Bitmap.createBitmap(
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
            if (saveAsWhiteCharacterOnBlack) {
                int alpha = Color.alpha(color);
                int red = 255 - Color.red(color);
                int green = 255 - Color.green(color);
                int blue = 255 - Color.blue(color);
                pixels[i] = Color.argb(alpha, red, green, blue);
            } else {
                pixels[i] = color;
            }
        }

        adjustedBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return adjustedBitmap;
    }

    private void processAndSegmentWord(Bitmap bitmap) {
        Bitmap adjustedBitmap = adjustBitmapColors(bitmap);

        Mat mat = new Mat();
        Utils.bitmapToMat(adjustedBitmap, mat);

        if (mat.channels() > 1) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        }

        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("TrainingActivity", "Contours found: " + contours.size());

        if (contours.isEmpty()) {
            Toast.makeText(this, "No characters found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Rect> boundingRects = new ArrayList<>();
        int minContourArea = 100; // Adjust as needed
        int imageArea = mat.rows() * mat.cols();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > minContourArea && contourArea < imageArea * 0.9) {
                boundingRects.add(rect);
            }
        }

        if (boundingRects.isEmpty()) {
            Toast.makeText(this, "No valid contours found after filtering.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalHeight = 0;
        for (Rect rect : boundingRects) {
            totalHeight += rect.height;
        }
        int avgCharHeight = totalHeight / boundingRects.size();
        int verticalTolerance = avgCharHeight / 2;

        Comparator<Rect> readingOrderComparator = new Comparator<Rect>() {
            @Override
            public int compare(Rect r1, Rect r2) {
                int r1CenterY = r1.y + r1.height / 2;
                int r2CenterY = r2.y + r2.height / 2;

                if (Math.abs(r1CenterY - r2CenterY) < verticalTolerance) {
                    return Integer.compare(r1.x, r2.x);
                } else {
                    return Integer.compare(r1.y, r2.y);
                }
            }
        };

        Collections.sort(boundingRects, readingOrderComparator);

        for (Rect rect : boundingRects) {
            Mat charMat = new Mat(mat, rect);

            Bitmap charBitmap = Bitmap.createBitmap(charMat.width(), charMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(charMat, charBitmap);

            Bitmap centeredBitmap = BitmapUtils.centerAndResizeBitmap(charBitmap, bitmapSize);

            addBitmapToSaveList(centeredBitmap);
        }

        Toast.makeText(this, "Extracted " + boundingRects.size() + " characters", Toast.LENGTH_SHORT).show();
    }


}
