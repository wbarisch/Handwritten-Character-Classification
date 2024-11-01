package com.example.hcc_elektrobit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.util.Consumer;

public class DialogManager {

    private final Activity activity;
    private final MainActivity mainActivity;
    private final ImageSharingManager imageSharingManager;
    private final ImageSavingManager imageSavingManager;

    public DialogManager(Activity activity, MainActivity mainActivity, ImageSharingManager imageSharingManager, ImageSavingManager imageSavingManager) {
        this.activity = activity;
        this.mainActivity = mainActivity;
        this.imageSharingManager = imageSharingManager;
        this.imageSavingManager = imageSavingManager;
    }

    public DialogManager(Activity activity) {
        this.activity = activity;
        this.mainActivity = null;
        this.imageSharingManager = null;
        this.imageSavingManager = null;
    }

    public void showShareOrSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose an action");

        String[] options = {"Share", "Save to Device"};

        builder.setItems(options, (dialog, which) -> {
            Bitmap currentBitmap = mainActivity.getBitmap();

            if (which == 0) {
                if (currentBitmap == null) {
                    Log.e("DialogManager", "Cannot share, bitmap is null");
                    Toast.makeText(activity, "No image to share!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("DialogManager", "Share Image option selected");
                    imageSharingManager.shareImage(currentBitmap);
                }
            } else if (which == 1) {
                if (currentBitmap == null) {
                    Log.e("DialogManager", "Cannot save, bitmap is null");
                    Toast.makeText(activity, "No image to save!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("DialogManager", "Save to Device option selected");
                    imageSavingManager.saveImageUsingDocumentIntent(currentBitmap);
                }
            }
        });

        builder.show();
    }

    public void showTrainingModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Training Mode");
        builder.setMessage("Do you want to enter Training Mode?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(activity, TrainingActivity.class);
            activity.startActivity(intent);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    public void showExitTrainingModeDialog(Runnable onConfirmExit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Exit Training Mode");
        builder.setMessage("Do you want to leave training mode?");
        builder.setPositiveButton("Yes", (dialog, which) -> onConfirmExit.run());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void showLeaveTestingDialog(Runnable onConfirmLeave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Stop Testing Characters");
        builder.setMessage("Do you want to stop testing characters and discard your data?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            onConfirmLeave.run();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void showNoImagesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("No Images")
                .setMessage("No images available to review.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void showKeepSelectedDialog(Runnable onConfirmSave, Runnable onCancelSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Keep Selected Images")
                .setMessage("Do you want to keep the selected images?")
                .setPositiveButton("Yes", (dialog, which) -> onConfirmSave.run())
                .setNegativeButton("No", (dialog, which) -> onCancelSave.run())
                .show();
    }

    public void showDeleteSelectedDialog(Runnable onConfirmDelete, Runnable onCancelDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Delete Selected Images")
                .setMessage("Are you sure you want to delete the selected images?")
                .setPositiveButton("Yes", (dialog, which) -> onConfirmDelete.run())
                .setNegativeButton("No", (dialog, which) -> onCancelDelete.run())
                .show();
    }

    public void showStrokeWidthInputDialog(DrawingCanvas drawingCanvas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Enter Stroke Width");

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                float strokeWidth = Float.parseFloat(input.getText().toString());
                drawingCanvas.setStrokeWidth(strokeWidth);
                Log.d("DialogManager", "Stroke width set to: " + strokeWidth);
            } catch (NumberFormatException e) {
                Toast.makeText(activity, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void showToggleBitmapModeDialog(boolean currentMode, Consumer<Boolean> onModeChanged) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Bitmap Mode");

        String[] options = {"White Character on Black", "Black Character on White"};
        int currentIndex = currentMode ? 0 : 1;

        builder.setSingleChoiceItems(options, currentIndex, (dialog, which) -> {
            boolean newMode = which == 0; // 0 for White on Black, 1 for Black on White
            onModeChanged.accept(newMode);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


}

