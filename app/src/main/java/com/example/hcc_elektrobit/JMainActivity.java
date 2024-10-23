package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class JMainActivity extends AppCompatActivity implements TimeoutActivity {

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private CNNonnxModel model;
    private Bitmap bitmap;
    private AudioPlayer audioPlayer;
    CanvasTimer canvasTimer;
    boolean timerStarted = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menuButton);
        item.setTitle("History");
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmain);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        Button shareButton = findViewById(R.id.share_button);
        Button trainingModeButton = findViewById(R.id.training_mode_button);
        Button siameseActivityButton = findViewById(R.id.siamese_test_button);
        Button supportsetActivityButton = findViewById(R.id.support_set_gen);


        model = new CNNonnxModel(this);
        audioPlayer = new AudioPlayer(this);

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
        ImageSavingManager imageSavingManager = new ImageSavingManager(createDocumentLauncher);

        DialogManager dialogManager = new DialogManager(this, this, imageSharingManager, imageSavingManager);

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
        }
        return super.onOptionsItemSelected(item);

    }

    public void onTimeout(){

        classifyCharacter();
        drawingCanvas.clear();
        timerStarted = false;

    }

    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap(28);

        if (bitmap == null) {
            Log.e("JMainActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        // TO DO:
        // - Call CharacterClassifier class
        // - To display the output character, set it to "recognizedCharTextView".

        int result = model.classifyAndReturnDigit(bitmap);

        History history = History.getInstance();
        HistoryItem historyItem = new HistoryItem(bitmap, result);

        history.saveItem(historyItem, this);

        bitmap = createBitmapFromFloatArray(model.preprocessBitmap(bitmap), 28, 28);
        audioPlayer.PlayAudio(String.valueOf(result));
        runOnUiThread(() -> {

            recognizedCharTextView.setText(String.valueOf(result));
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

    public void enterTrainingMode() {
        Log.d("JMainActivity", "Training mode enabled");
    }

}