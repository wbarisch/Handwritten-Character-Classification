package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class SiameseTesterActivity extends AppCompatActivity implements TimeoutActivity {

    private static final String TAG = "SiameseTesterActivity";

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private ImageView bitmapDisplay2;
    private SMSonnxModel model;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Timer canvasTimer;
    private boolean timerStarted = false;
    int bitmapState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siamesetester);

        model = SMSonnxModel.getInstance(this);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        bitmapDisplay2 = findViewById(R.id.bitmap_display2);
        model = SMSonnxModel.getInstance(this);

        SupportSet.getInstance().updateSet();

        drawingCanvas.setOnTouchListener((v, event) -> {

            if (timerStarted) {
                canvasTimer.cancel();
                timerStarted = false;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                canvasTimer = new Timer(this, 1000);
                new Thread(canvasTimer).start();
                timerStarted = true;
                v.performClick();
            }

            return false;
        });
    }


    public void onTimeout(){

        findSimilarity();
        drawingCanvas.clear();
        timerStarted = false;
    }

    private void findSimilarity() {
        if (bitmapState == 0) {
            bitmap = drawingCanvas.getBitmap(105, true);
            runOnUiThread(() -> {
                bitmapDisplay2.setImageDrawable(null);
                bitmapDisplay.setImageBitmap(createBitmapFromPreprocessedData(model.preprocessBitmap(bitmap)));
                recognizedCharTextView.setText("_");
            });
            bitmapState = 1;
        } else {
            bitmap2 = drawingCanvas.getBitmap(105, true);

            try {
                float similarityScore = model.findSimilarity(bitmap, bitmap2);

                runOnUiThread(() -> {
                    bitmapDisplay2.setImageBitmap(createBitmapFromPreprocessedData(model.preprocessBitmap(bitmap2)));
                    recognizedCharTextView.setText("Similarity Score: " + similarityScore);
                    Log.i(TAG, "Similarity Score between bitmap1 and bitmap2: " + similarityScore);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error finding similarity", e);
                runOnUiThread(() -> {
                    recognizedCharTextView.setText("Error computing similarity");
                });
            }

            bitmapState = 0;
        }
    }
    public Bitmap createBitmapFromPreprocessedData(float[][][][] data) {
        int width = data[0][0][0].length;
        int height = data[0][0].length;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = data[0][0][y][x];
                value = Math.max(0, Math.min(1, value));
                int grayscale = (int) (value * 255);
                int color = Color.argb(255, grayscale, grayscale, grayscale);
                bitmap.setPixel(x, y, color);
            }
        }
        return bitmap;
    }


    public Bitmap getBitmap() {
            return bitmap;
        }

}