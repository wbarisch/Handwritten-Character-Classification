package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.databinding.DataBindingUtil;
import com.example.hcc_elektrobit.databinding.ActivityMainBinding;
import java.io.IOException;
import java.io.OutputStream;

public class JMainActivity extends AppCompatActivity {

    private DrawingCanvas drawingCanvas;
    private TextView recognizedCharTextView;
    private ImageView bitmapDisplay;
    private Bitmap bitmap;
    Timer canvasTimer;                            // Must go to MainViewModel
    boolean timerStarted = false;
    private MainViewModel viewModel;

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
        viewModel = new MainViewModel(this.getApplication());
        setContentView(R.layout.activity_jmain);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_jmain);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        Button shareButton = findViewById(R.id.share_button);
        Button trainingModeButton = findViewById(R.id.training_mode_button);
        Button siameseActivityButton = findViewById(R.id.siamese_test_button);

        siameseActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to SiameseTesterActivity
                Intent intent = new Intent(JMainActivity.this, SiameseTesterActivity.class);
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

        viewModel.clearCanvasEvent.observe(this, shouldClear -> {
            if (shouldClear) {
                bitmap = drawingCanvas.getBitmap(28);
                viewModel.mainAppFunction(bitmap);
                drawingCanvas.clear();
                viewModel.clearCanvasHandled(); // Reset the event state in the ViewModel
            }
        });

        drawingCanvas.setOnTouchListener((v, event) -> {

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                bitmap = drawingCanvas.getBitmap(28);
                viewModel.startTimer(1000);
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

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void enterTrainingMode() {
        Log.d("JMainActivity", "Training mode enabled");
    }

}