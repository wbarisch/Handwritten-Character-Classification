package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JMainActivity extends AppCompatActivity implements TimeoutActivity {

    private DialogManager dialogManager;
    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private SMSonnxModel model;
    private Bitmap bitmap;
    private AudioPlayer audioPlayer;
    CanvasTimer canvasTimer;
    private CharacterMapping characterMapping;
    boolean timerStarted = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem historyItem = menu.findItem(R.id.menuButton);
        if (historyItem != null) {
            historyItem.setTitle("History");
        }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmain);

        characterMapping = new CharacterMapping();
        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        Button shareButton = findViewById(R.id.share_button);
        Button trainingModeButton = findViewById(R.id.training_mode_button);
        Button siameseActivityButton = findViewById(R.id.siamese_test_button);
        Button supportsetActivityButton = findViewById(R.id.support_set_gen);


        model = SMSonnxModel.getInstance(this);
        audioPlayer = new AudioPlayer(this);
        SupportSet.getInstance().updateSet(this);

        siameseActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to SiameseTesterActivity
                Intent intent = new Intent(JMainActivity.this, SiameseTesterActivity.class);
                startActivity(intent);
            }
        });

        supportsetActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to SiameseTesterActivity
                Intent intent = new Intent(JMainActivity.this, SupportSetActivity.class);
                startActivity(intent);
            }
        });

        ActivityResultLauncher<Intent> createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                                if (out != null) {
                                    ImageSavingManager.saveBitmapAsBMP(bitmap, out);
                                    Log.d("SaveImage", "Image saved to: " + uri);
                                } else {
                                    Log.e("SaveImage", "'out' OutputStream is null");
                                }
                            } catch (IOException e) {
                                Log.e("SaveImage", "Failed to save the image.", e);
                            }
                        }
                    }
                }
        );

        ImageSharingManager imageSharingManager = new ImageSharingManager(this);
        ImageSavingManager imageSavingManager = new ImageSavingManager(createDocumentLauncher, characterMapping);


        dialogManager = new DialogManager(this, this, imageSharingManager, imageSavingManager);

        shareButton.setOnClickListener(v -> dialogManager.showShareOrSaveDialog());
        trainingModeButton.setOnClickListener(v -> dialogManager.showTrainingModeDialog());

        drawingCanvas.setOnTouchListener((v, event) -> {

            if(timerStarted){
                canvasTimer.cancel();
                timerStarted = false;
            }

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                bitmap = drawingCanvas.getBitmap(28);
                canvasTimer = new CanvasTimer(this);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }

            return true;
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.menuButton) {
            Intent intent = new Intent(JMainActivity.this, JHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_toggle_bitmap_method) {
            item.setChecked(!item.isChecked());

            if (drawingCanvas != null) {
                drawingCanvas.setUseOldBitmapMethod(item.isChecked());
            }

            invalidateOptionsMenu();

            Log.d("JMainActivity", "Use Old Bitmap Method set to: " + item.isChecked());
            return true;
        } else if (id == R.id.action_toggle_antialias) {
            if (!drawingCanvas.isUseOldBitmapMethod()) {
                item.setChecked(!item.isChecked());
                if (drawingCanvas != null) {
                    drawingCanvas.setAntiAlias(item.isChecked());
                }
                Log.d("JMainActivity", "Anti-Alias set to: " + item.isChecked());
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

    public void onTimeout(){

        classifyCharacter();
        drawingCanvas.clear();
        timerStarted = false;

    }

    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap(105);

        if (bitmap == null) {
            Log.e("JMainActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        Pair<String, Map<String, Float>> result_pair = model.classifyAndReturnPredAndSimilarityMap(bitmap);

        History history = History.getInstance();
        SMSHistoryItem historyItem = new SMSHistoryItem(bitmap, result_pair.first, result_pair.second);

        history.saveItem(historyItem, this);
        String result = result_pair.first;
        Log.e("MAIN", result);
        Log.e("Main", result_pair.second.toString());
        //bitmap = createBitmapFromFloatArray(model.preprocessBitmap(bitmap), 28, 28);
        audioPlayer.PlayAudio(result);
        runOnUiThread(() -> {

            recognizedCharTextView.setText(result);
            bitmapDisplay.setImageBitmap(bitmap);

        });
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
            int color = Color.argb(255, grayscale, grayscale, grayscale);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}