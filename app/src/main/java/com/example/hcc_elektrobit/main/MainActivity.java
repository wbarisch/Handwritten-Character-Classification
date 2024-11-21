package com.example.hcc_elektrobit.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.example.hcc_elektrobit.evaluation.JEvaluationActivity;
import com.example.hcc_elektrobit.utils.CharacterMapping;
import com.example.hcc_elektrobit.utils.DialogManager;
import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.driving_mode.DrivingMode;
import com.example.hcc_elektrobit.utils.ImageSavingManager;
import com.example.hcc_elektrobit.utils.ImageSharingManager;
import com.example.hcc_elektrobit.keyboard_mode.KeyboardModeActivity;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.evaluation.SiameseTesterActivity;
import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.support_set.SupportSetActivity;
import com.example.hcc_elektrobit.databinding.ActivityJmainBinding;
import com.example.hcc_elektrobit.databinding.ActivityMainBinding;
import com.example.hcc_elektrobit.history.JHistoryActivity;
import com.example.hcc_elektrobit.history.History;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private CharacterMapping characterMapping = new CharacterMapping();
    private DialogManager dialogManager;
    private DrawingCanvas drawingCanvas;
    private Bitmap bitmap;
    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem historyItem = menu.findItem(R.id.menuButton);
        if (historyItem != null) {
            historyItem.setTitle("History");
        }
        MenuItem driverMode = menu.findItem(R.id.driver_mode);
        MenuItem keyboardMode = menu.findItem(R.id.keyboard_mode);
        MenuItem evalMode = menu.findItem(R.id.evalutation_mode);

        MenuItem canvasSettingsItem = menu.findItem(R.id.action_canvas_settings);

        canvasSettingsItem.setOnMenuItemClickListener(menuItem -> {
            showCanvasSettingsDialog();
            return true;
        });

        keyboardMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, KeyboardModeActivity.class);
                startActivity(intent);
                return true;
            }
        });

        driverMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, DrivingMode.class);
                startActivity(intent);
                return true;
            }
        });

        evalMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, JEvaluationActivity.class);
                startActivity(intent);
                return true;
            }
        });



        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new MainViewModel(this.getApplication());

        ActivityJmainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_jmain);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        Button shareButton = findViewById(R.id.share_button);
        Button trainingModeButton = findViewById(R.id.training_mode_button);
        Button supportsetActivityButton = findViewById(R.id.support_set_gen);

        SupportSet.getInstance().updateSet();
        History.getInstance().updateHistoryFromCache();


        supportsetActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SupportSetActivity.class);
            startActivity(intent);
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

        drawingCanvas.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawingCanvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                viewModel.clearCanvasEvent.observe(MainActivity.this, shouldClear -> {
                    if (shouldClear) {
                        bitmap = drawingCanvas.getBitmap(105, true, 3f);
                        viewModel.mainAppFunction(bitmap);
                        drawingCanvas.clear();
                        viewModel.clearCanvasHandled();
                    }
                });

                viewModel.getBitmapSize().observe(MainActivity.this, size -> {
                    bitmap = drawingCanvas.getBitmap(size, true);
                });

                drawingCanvas.setOnTouchListener((v, event) -> {
                    drawingCanvas.onTouchEvent(event);

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        bitmap = drawingCanvas.getBitmap(105, true);
                        viewModel.startTimer(1000);
                        v.performClick();
                    }

                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        viewModel.stopTimer();
                    }

                    return true;
                });
            }
        });
    }

    private void showCanvasSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Canvas Settings");

        View settingsView = getLayoutInflater().inflate(R.layout.dialog_canvas_settings, null);

        EditText strokeWidthInput = settingsView.findViewById(R.id.stroke_width_input);
        Switch antiAliasSwitch = settingsView.findViewById(R.id.anti_alias_switch);

        if (drawingCanvas != null) {
            strokeWidthInput.setText(String.valueOf(drawingCanvas.getCurrentStrokeWidth()));
            antiAliasSwitch.setChecked(drawingCanvas.getPaint().isAntiAlias());
        }

        builder.setView(settingsView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button confirmButton = settingsView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            if (drawingCanvas != null) {
                int newStrokeWidth = Integer.parseInt(strokeWidthInput.getText().toString());

                drawingCanvas.setStrokeWidth(newStrokeWidth);
                drawingCanvas.setAntiAlias(antiAliasSwitch.isChecked());
            }

            dialog.dismiss();
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == R.id.menuButton) {

            Intent intent = new Intent(MainActivity.this, JHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }
        return true;
    }



    public Bitmap getBitmap() {
        return bitmap;
    }

    private void showBitmapSizeInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Bitmap Size");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newSize = Integer.parseInt(input.getText().toString());
                viewModel.setBitmapSize(newSize);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void enterTrainingMode() {
        Log.d("JMainActivity", "Training mode enabled");
    }
}