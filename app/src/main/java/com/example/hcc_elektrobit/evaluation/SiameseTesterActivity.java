package com.example.hcc_elektrobit.evaluation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hcc_elektrobit.model.SMSComaparison;
import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;

public class SiameseTesterActivity extends AppCompatActivity implements TimeoutActivity {

    private static final String TAG = "SiameseTesterActivity";

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private ImageView bitmapDisplay2;
    private SMSComaparison model;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Timer canvasTimer;
    private boolean timerStarted = false;
    int bitmapState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siamesetester);

        model = SMSComaparison.getInstance();

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        bitmapDisplay2 = findViewById(R.id.bitmap_display2);

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
                bitmapDisplay.setImageBitmap(bitmap);
                recognizedCharTextView.setText("_");
            });
            bitmapState = 1;
        } else {
            bitmap2 = drawingCanvas.getBitmap(105, true);

            try {

                float similarityScore = model.computeCosineSimilarityBitmap(bitmap, bitmap2);

                runOnUiThread(() -> {
                    bitmapDisplay2.setImageBitmap(bitmap2);
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


    public Bitmap getBitmap() {
            return bitmap;
        }

}