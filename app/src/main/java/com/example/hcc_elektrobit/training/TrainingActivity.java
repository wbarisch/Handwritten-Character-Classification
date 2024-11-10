package com.example.hcc_elektrobit.training;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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


import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;
import com.example.hcc_elektrobit.history.JHistoryActivity;
import com.example.hcc_elektrobit.utils.CharacterMapping;
import com.example.hcc_elektrobit.utils.DialogManager;
import com.example.hcc_elektrobit.utils.ImageSavingManager;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TrainingActivity extends AppCompatActivity implements TimeoutActivity {
    private static final int REVIEW_IMAGES_REQUEST = 1;

    private int bitmapSize = 28;

    private Bitmap bitmap;
    private Timer canvasTimer;
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

        canvasTimer = new Timer(this, 1000);

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
            dialogManager.showToggleBitmapModeDialog(saveAsWhiteCharacterOnBlack, newMode -> {
                saveAsWhiteCharacterOnBlack = newMode;
                String mode = saveAsWhiteCharacterOnBlack ? "White Character on Black" : "Black Character on White";
                Toast.makeText(this, "Bitmap mode set to: " + mode, Toast.LENGTH_SHORT).show();
            });
            return true;
        } else if (id == R.id.action_toggle_antialias) {
            item.setChecked(!item.isChecked());
            if (drawingCanvas != null) {
                drawingCanvas.setAntiAlias(item.isChecked());
            }
            Log.d("TrainingActivity", "Anti-Alias set to: " + item.isChecked());
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

        boolean invertedForProcessing = false;
        if (!saveAsWhiteCharacterOnBlack) {
            Core.bitwise_not(mat, mat);
            invertedForProcessing = true;
        }

        int padding = 5;
        Core.copyMakeBorder(mat, mat, padding, padding, padding, padding, Core.BORDER_CONSTANT, new Scalar(0));

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
        int imageArea = mat.rows() * mat.cols();
        double minContourArea = imageArea * 0.0001;

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

        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (Rect rect : boundingRects) {
            if (rect.x < left) left = rect.x;
            if (rect.y < top) top = rect.y;
            if (rect.x + rect.width > right) right = rect.x + rect.width;
            if (rect.y + rect.height > bottom) bottom = rect.y + rect.height;
        }

        int expansion = 10;
        left = Math.max(left - expansion, 0);
        top = Math.max(top - expansion, 0);
        right = Math.min(right + expansion, mat.cols());
        bottom = Math.min(bottom + expansion, mat.rows());

        Rect writingArea = new Rect(left, top, right - left, bottom - top);
        mat = new Mat(mat, writingArea);

        for (int i = 0; i < boundingRects.size(); i++) {
            Rect rect = boundingRects.get(i);
            rect.x -= left;
            rect.y -= top;
            boundingRects.set(i, rect);
        }
        List<Integer> heights = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        for (Rect rect : boundingRects) {
            heights.add(rect.height);
            widths.add(rect.width);
        }
        Collections.sort(heights);
        Collections.sort(widths);
        int medianIndex = heights.size() / 2;
        double medianHeight = heights.get(medianIndex);
        double medianWidth = widths.get(medianIndex);

        double dotSizeThreshold = medianHeight * 0.3;

        List<Rect> mainBodies = new ArrayList<>();
        List<Rect> dots = new ArrayList<>();

        for (Rect rect : boundingRects) {
            double aspectRatio = (double) rect.width / rect.height;
            if (rect.height < dotSizeThreshold && rect.width < dotSizeThreshold && aspectRatio >= 0.5 && aspectRatio <= 1.5) {
                dots.add(rect);
            } else {
                mainBodies.add(rect);
            }
        }

        Map<Integer, Rect> mergedRectsMap = new HashMap<>();
        for (int i = 0; i < mainBodies.size(); i++) {
            mergedRectsMap.put(i, mainBodies.get(i));
        }
        boolean[] dotMerged = new boolean[dots.size()];

        for (int i = 0; i < dots.size(); i++) {
            Rect dotRect = dots.get(i);
            int dotCenterX = dotRect.x + dotRect.width / 2;

            double minDistance = Double.MAX_VALUE;
            int bestIndex = -1;

            for (int j = 0; j < mainBodies.size(); j++) {
                Rect mainRect = mainBodies.get(j);

                double verticalGap = mainRect.y - (dotRect.y + dotRect.height);
                double verticalGapThreshold = mainRect.height * 1.5;

                if (verticalGap >= -mainRect.height * 0.1 && verticalGap <= verticalGapThreshold) {
                    int mainCenterX = mainRect.x + mainRect.width / 2;
                    int horizontalDistance = Math.abs(mainCenterX - dotCenterX);
                    int maxHorizontalDistance = (int) (mainRect.width * 1.5);

                    if (horizontalDistance <= maxHorizontalDistance) {
                        double distance = Math.hypot(horizontalDistance, verticalGap);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestIndex = j;
                        }
                    }
                }
            }

            if (bestIndex != -1) {
                Rect mainRect = mergedRectsMap.get(bestIndex);
                Rect combinedRect = unionRect(mainRect, dotRect);
                mergedRectsMap.put(bestIndex, combinedRect);
                dotMerged[i] = true;
            }
        }

        List<Rect> mergedRects = new ArrayList<>(mergedRectsMap.values());

        for (int i = 0; i < dots.size(); i++) {
            if (!dotMerged[i]) {
                mergedRects.add(dots.get(i));
            }
        }
        mergedRects = mergeNearbyComponents(mergedRects);

        drawBoundingRects(mat, mergedRects, "Merged Bounding Rects");

        int totalHeight = 0;
        for (Rect rect : mergedRects) {
            totalHeight += rect.height;
        }
        double avgCharHeight = (double) totalHeight / mergedRects.size();

        List<List<Rect>> lines = new ArrayList<>();
        mergedRects.sort(Comparator.comparingInt(r -> r.y));
        double lineThreshold = avgCharHeight * 0.7;

        List<Rect> currentLine = new ArrayList<>();
        Rect previousRect = null;
        for (Rect rect : mergedRects) {
            if (previousRect == null) {
                currentLine.add(rect);
            } else {
                if (Math.abs(rect.y - previousRect.y) <= lineThreshold) {
                    currentLine.add(rect);
                } else {
                    lines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentLine.add(rect);
                }
            }
            previousRect = rect;
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        List<Rect> sortedRects = new ArrayList<>();
        for (List<Rect> line : lines) {
            line.sort(Comparator.comparingInt(r -> r.x));
            sortedRects.addAll(line);
        }

        for (Rect rect : sortedRects) {
            int adjustedX = rect.x;
            int adjustedY = rect.y;
            int adjustedWidth = rect.width;
            int adjustedHeight = rect.height;

            if (adjustedX + adjustedWidth > mat.cols()) {
                adjustedWidth = mat.cols() - adjustedX;
            }
            if (adjustedY + adjustedHeight > mat.rows()) {
                adjustedHeight = mat.rows() - adjustedY;
            }

            Rect adjustedRect = new Rect(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
            Mat charMat = new Mat(mat, adjustedRect);

            if (invertedForProcessing) {
                Core.bitwise_not(charMat, charMat);
            }

            Bitmap centeredBitmap = centerAndResizeCharMat(charMat, bitmapSize);
            addBitmapToSaveList(centeredBitmap);
        }

        Toast.makeText(this, "Extracted " + sortedRects.size() + " characters", Toast.LENGTH_SHORT).show();
    }

    private List<Rect> mergeNearbyComponents(List<Rect> rects) {

        List<Integer> heights = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        for (Rect rect : rects) {
            heights.add(rect.height);
            widths.add(rect.width);
        }
        Collections.sort(heights);
        Collections.sort(widths);
        double medianHeight = heights.get(heights.size() / 2);
        double medianWidth = widths.get(widths.size() / 2);

        double maxHorizontalGap = medianWidth * 0.15;
        double maxVerticalGap = medianHeight * 0.15;

        boolean[] merged = new boolean[rects.size()];
        List<Rect> mergedRects = new ArrayList<>();

        for (int i = 0; i < rects.size(); i++) {
            if (merged[i]) continue;

            Rect baseRect = rects.get(i);
            Rect combinedRect = new Rect(baseRect.x, baseRect.y, baseRect.width, baseRect.height);
            merged[i] = true;

            for (int j = i + 1; j < rects.size(); j++) {
                if (merged[j]) continue;

                Rect compareRect = rects.get(j);

                if (areRectsClose(baseRect, compareRect, maxHorizontalGap, maxVerticalGap)) {

                    Rect tempCombinedRect = unionRect(combinedRect, compareRect);
                    double maxAllowedWidth = medianWidth * 1.5;
                    double maxAllowedHeight = medianHeight * 2.0;
                    if (tempCombinedRect.width <= maxAllowedWidth && tempCombinedRect.height <= maxAllowedHeight) {
                        combinedRect = tempCombinedRect;
                        merged[j] = true;
                    }
                }
            }
            mergedRects.add(combinedRect);
        }
        return mergedRects;
    }

    private boolean areRectsClose(Rect r1, Rect r2, double maxHGap, double maxVGap) {
        int hDistance = Math.abs((r1.x + r1.width / 2) - (r2.x + r2.width / 2)) - (r1.width + r2.width) / 2;
        int vDistance = Math.abs((r1.y + r1.height / 2) - (r2.y + r2.height / 2)) - (r1.height + r2.height) / 2;

        return hDistance <= maxHGap && vDistance <= maxVGap;
    }

    private Rect unionRect(Rect rectA, Rect rectB) {
        int x = Math.min(rectA.x, rectB.x);
        int y = Math.min(rectA.y, rectB.y);
        int width = Math.max(rectA.x + rectA.width, rectB.x + rectB.width) - x;
        int height = Math.max(rectA.y + rectA.height, rectB.y + rectB.height) - y;
        return new Rect(x, y, width, height);
    }
    private void drawBoundingRects(Mat mat, List<Rect> rects, String windowName) {
        Mat matCopy = mat.clone();

        for (Rect rect : rects) {
            Imgproc.rectangle(matCopy, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
        }

        Bitmap bitmap = Bitmap.createBitmap(matCopy.cols(), matCopy.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matCopy, bitmap);

    }

    private Bitmap centerAndResizeCharMat(Mat charMat, int desiredSize) {
        Mat nonZeroCoordinates = new Mat();
        Core.findNonZero(charMat, nonZeroCoordinates);

        if (nonZeroCoordinates.empty()) {
            Bitmap emptyBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
            Canvas emptyCanvas = new Canvas(emptyBitmap);
            emptyCanvas.drawColor(Color.BLACK);
            return emptyBitmap;
        }

        Rect bbox = Imgproc.boundingRect(nonZeroCoordinates);
        Mat cropped = new Mat(charMat, bbox);

        int contentWidth = cropped.cols();
        int contentHeight = cropped.rows();
        int margin = 2;
        int maxContentSize = desiredSize - 2 * margin;
        float scale = (float) maxContentSize / Math.max(contentWidth, contentHeight);
        scale = Math.min(scale, 1.0f);

        int newWidth = Math.round(contentWidth * scale);
        int newHeight = Math.round(contentHeight * scale);
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        Size newSize = new Size(newWidth, newHeight);
        Mat resizedChar = new Mat();
        Imgproc.resize(cropped, resizedChar, newSize, 0, 0, Imgproc.INTER_AREA);

        Mat outputMat = Mat.zeros(desiredSize, desiredSize, charMat.type());

        int x = (desiredSize - newWidth) / 2;
        int y = (desiredSize - newHeight) / 2;
        Rect roi = new Rect(x, y, newWidth, newHeight);
        resizedChar.copyTo(outputMat.submat(roi));

        Bitmap centeredBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, centeredBitmap);
        return centeredBitmap;
    }
}