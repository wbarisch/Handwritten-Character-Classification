package com.example.hcc_elektrobit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSharingManager {

    private final Activity activity;

    public ImageSharingManager(Activity activity) {
        this.activity = activity;
    }

    public void shareImage(Bitmap bitmap) {
        if (bitmap == null) return;

        try {
            File dir = new File(activity.getFilesDir(), "BitmapImages");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    Log.e("ShareImage", "Failed to create directory.");
                    return;
                }
            }

            String fileName = "shared_image_" + System.currentTimeMillis() + ".bmp";
            File imageFile = new File(dir, fileName);

            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                ImageSavingManager.saveBitmapAsBMP(bitmap, out);
                Log.d("ShareImage", "Image saved to: " + imageFile.getAbsolutePath());
            }

            Uri contentUri = FileProvider.getUriForFile(activity, "com.example.hcc_elektrobit.fileprovider", imageFile);
            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/bmp");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(shareIntent, "Share image via"));
            }

        } catch (IOException e) {
            Log.e("ShareImage", "Failed to save the image.", e);
        }
    }
}
