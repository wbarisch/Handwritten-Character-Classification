package com.example.hcc_elektrobit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

public class DialogManager {

    private final Activity activity;
    private final JMainActivity jMainActivity;
    private final ImageSharingManager imageSharingManager;
    private final ImageSavingManager imageSavingManager;

    public DialogManager(Activity activity, JMainActivity jMainActivity, ImageSharingManager imageSharingManager, ImageSavingManager imageSavingManager) {
        this.activity = activity;
        this.jMainActivity = jMainActivity;
        this.imageSharingManager = imageSharingManager;
        this.imageSavingManager = imageSavingManager;
    }

    public DialogManager(Activity activity) {
        this.activity = activity;
        this.jMainActivity = null;
        this.imageSharingManager = null;
        this.imageSavingManager = null;
    }

    public void showShareOrSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose an action");

        String[] options = {"Share", "Save to Device"};

        builder.setItems(options, (dialog, which) -> {
            Bitmap currentBitmap = jMainActivity.getBitmap();

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

    public void showDiscardAllDialog(Runnable onConfirmDiscard, Runnable onCancelDiscard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Discard All Images")
                .setMessage("Do you want to discard all the images?")
                .setPositiveButton("Yes", (dialog, which) -> onConfirmDiscard.run())
                .setNegativeButton("No", (dialog, which) -> onCancelDiscard.run())
                .show();
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


}

