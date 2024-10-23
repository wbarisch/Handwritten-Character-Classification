package com.example.hcc_elektrobit;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageSavingManager {

    private final ActivityResultLauncher<Intent> documentLauncher;

    public ImageSavingManager(ActivityResultLauncher<Intent> documentLauncher) {
        this.documentLauncher = documentLauncher;
    }

    public void saveImageUsingDocumentIntent(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("ImageSavingManager", "Bitmap is null, cannot save");
            return;
        }

        Log.d("ImageSavingManager", "Saving image using document intent");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/bmp");
        intent.putExtra(Intent.EXTRA_TITLE, "image_" + System.currentTimeMillis() + ".bmp");

        documentLauncher.launch(intent);
    }

    public void saveImageToDevice(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("ImageSavingManager", "Bitmap is null, cannot save");
            return;
        }

        saveImageToPublicStorage(context, bitmap);
    }

    public void saveImageToCharacterFolder(Context context, Bitmap bitmap, String character, String filename) {
        if (bitmap == null || character == null || filename == null) {
            Log.e("ImageSavingManager", "Invalid input for saving image to character folder");
            return;
        }

        String directoryPath = Environment.DIRECTORY_PICTURES + "/TrainingImages/" + character;
        String fileName = character + "_" + System.currentTimeMillis() + ".bmp";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/bmp");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, directoryPath);

        try (OutputStream fos = context.getContentResolver()
                .openOutputStream(context.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))) {

            if (fos != null) {
                saveBitmapAsBMP(bitmap, fos);
                fos.flush();
                Log.d("ImageSavingManager", "Image saved to public storage in folder: " + directoryPath);
            } else {
                Log.e("ImageSavingManager", "Failed to open output stream for public storage");
            }
        } catch (IOException e) {
            Log.e("ImageSavingManager", "Error saving image to public storage", e);
        }
    }


    public void saveBitmapToCache(Context context, Bitmap bitmap, String fileName) {
        if (bitmap == null) {
            Log.e("ImageSavingManager", "Bitmap is null, cannot save");
            return;
        }

        File cacheDir = context.getCacheDir();
        File imageFile = new File(cacheDir, fileName + ".bmp");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            saveBitmapAsBMP(bitmap, fos);
            fos.flush();
            Log.d("ImageSavingManager", "Image saved to cache: " + imageFile.getPath());
        } catch (IOException e) {
            Log.e("ImageSavingManager", "Error saving image to cache", e);
        }
    }



    public void saveImageToPublicStorage(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("ImageSavingManager", "Bitmap is null, cannot save");
            return;
        }

        String fileName = "image_" + System.currentTimeMillis() + ".bmp";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/bmp");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TrainingImages");

        try {
            OutputStream fos = context.getContentResolver()
                    .openOutputStream(context.getContentResolver()
                            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));

            if (fos != null) {
                saveBitmapAsBMP(bitmap, fos);  // Save the bitmap as BMP
                fos.flush();
                fos.close();
                Log.d("ImageSavingManager", "Image saved and added to MediaStore.");
            }
        } catch (IOException e) {
            Log.e("ImageSavingManager", "Error saving image to public storage", e);
        }
    }

    public void deleteAllImages(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.isDirectory()) {
            for (File file : cacheDir.listFiles()) {
                if (file.getName().endsWith(".bmp")) {
                    file.delete();
                }
            }
        }
        File externalDir = new File(context.getExternalFilesDir(null), "TrainingImages");
        if (externalDir.isDirectory()) {
            for (File characterDir : externalDir.listFiles()) {
                if (characterDir.isDirectory()) {
                    for (File file : characterDir.listFiles()) {
                        if (file.getName().endsWith(".bmp")) {
                            file.delete();
                        }
                    }
                }
            }
        }

        Log.d("ImageSavingManager", "All images deleted from cache and external storage");
    }



    public static void saveBitmapAsBMP(Bitmap bitmap, OutputStream out) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int paddedRowSize = (width * 3 + 3) & (~3);
        int bmpSize = paddedRowSize * height;

        out.write(0x42); out.write(0x4D);
        out.write(intToByteArray(14 + 40 + bmpSize));
        out.write(new byte[4]);  // Reserved
        out.write(intToByteArray(14 + 40));

        out.write(intToByteArray(40));
        out.write(intToByteArray(width));
        out.write(intToByteArray(height));
        out.write(new byte[]{0x01, 0x00});
        out.write(new byte[]{0x18, 0x00});
        out.write(new byte[4]);
        out.write(intToByteArray(bmpSize));
        out.write(new byte[16]);

        byte[] row = new byte[paddedRowSize];
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                row[x * 3] = (byte) (pixel & 0xFF);
                row[x * 3 + 1] = (byte) ((pixel >> 8) & 0xFF);
                row[x * 3 + 2] = (byte) ((pixel >> 16) & 0xFF);
            }
            out.write(row);
        }
    }

    private static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }
}


