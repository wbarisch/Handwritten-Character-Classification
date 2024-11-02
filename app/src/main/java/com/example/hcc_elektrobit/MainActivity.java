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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.example.hcc_elektrobit.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private CharacterMapping characterMapping = new CharacterMapping();
    private DialogManager dialogManager;
    private DrawingCanvas drawingCanvas;
    private Bitmap bitmap;
    private MainViewModel viewModel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem historyItem = menu.findItem(R.id.menuButton);
        if (historyItem != null) {
            historyItem.setTitle("History");
        }
        MenuItem toggleAntiAliasItem = menu.findItem(R.id.action_toggle_antialias);
        MenuItem selectStrokeWidthItem = menu.findItem(R.id.action_select_stroke_width);
        MenuItem driverMode = menu.findItem(R.id.driver_mode);

        driverMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, DrivingMode.class);
                startActivity(intent);
                return true;
            }
        });

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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        viewModel = new MainViewModel(this.getApplication());
        setContentView(R.layout.activity_jmain);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_jmain);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        Button shareButton = findViewById(R.id.share_button);
        Button trainingModeButton = findViewById(R.id.training_mode_button);
        Button siameseActivityButton = findViewById(R.id.siamese_test_button);
        Button supportsetActivityButton = findViewById(R.id.support_set_gen);

        SupportSet.getInstance().updateSet();

        siameseActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to SiameseTesterActivity
                Intent intent = new Intent(MainActivity.this, SiameseTesterActivity.class);
                startActivity(intent);
            }
        });

        supportsetActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SupportSetActivity.class);
                startActivity(intent);
            }
        });

        // No idea what this does
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

        // Check this ImageSavingManager
        ImageSharingManager imageSharingManager = new ImageSharingManager(this);
        ImageSavingManager imageSavingManager = new ImageSavingManager(createDocumentLauncher, characterMapping);

        // Check DialogManager
        dialogManager = new DialogManager(this, this, imageSharingManager, imageSavingManager);
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

        viewModel.isCnnModel.observe(this, newValue -> {
            viewModel.onCnnSwitchChanged();
        });

        viewModel.isQuantizedModel.observe(this, newValue -> {
            viewModel.onQuanSwitchToggled();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        if(id == R.id.menuButton) {

            Intent intent = new Intent(MainActivity.this, JHistoryActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.action_toggle_antialias) {
            item.setChecked(!item.isChecked());
            if (drawingCanvas != null) {
                drawingCanvas.setAntiAlias(item.isChecked());
            }
            Log.d("JMainActivity", "Anti-Alias set to: " + item.isChecked());
            return true;
        } else if (id == R.id.action_select_stroke_width) {
            dialogManager.showStrokeWidthInputDialog(drawingCanvas);
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