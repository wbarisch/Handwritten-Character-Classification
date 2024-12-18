package com.example.hcc_elektrobit.utils;

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
import java.util.ArrayList;
import java.util.List;

public class ImageSavingManager {

    private final ActivityResultLauncher<Intent> documentLauncher;
    private final CharacterMapping characterMapping;

    public ImageSavingManager(ActivityResultLauncher<Intent> documentLauncher, CharacterMapping characterMapping) {
        this.documentLauncher = documentLauncher;
        this.characterMapping = characterMapping;
    }

    private int getMaxImageIndex(Context context, int characterId) {
        String characterFolderName = characterMapping.getPaddedId(characterId);
        String directoryPath = Environment.DIRECTORY_PICTURES + "/TrainingImages/" + characterFolderName;
        File directory = new File(Environment.getExternalStoragePublicDirectory(directoryPath).toString());
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        File[] files = directory.listFiles();
        int maxIndex = 0;

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("") && fileName.endsWith(".bmp")) {
                    String indexPart = fileName.substring(0, fileName.lastIndexOf("."));
                    try {
                        int index = Integer.parseInt(indexPart);
                        if (index > maxIndex) {
                            maxIndex = index;
                        }
                    } catch (NumberFormatException e) {
                        Log.e("ImageSavingManager", "Error parsing file index: " + fileName);
                    }
                }
            }
        }

        return maxIndex;
    }

    public void saveSelectedImages(Context context, List<Bitmap> selectedBitmaps, int characterId) {
        if (selectedBitmaps == null || selectedBitmaps.isEmpty()) {
            Log.e("ImageSavingManager", "No images selected to save.");
            return;
        }

        int currentMaxIndex = getMaxImageIndex(context, characterId);
        for (Bitmap bitmap : selectedBitmaps) {
            if (bitmap != null) {
                currentMaxIndex++;
                String filename = String.format("%03d.bmp", currentMaxIndex);
                saveImageToCharacterFolder(context, bitmap, characterId, filename);
            }
        }

        Log.d("ImageSavingManager", "Selected images saved to character folder: " + characterId);
    }

    public void deleteSelectedImages(Context context, List<Bitmap> selectedBitmaps, List<Bitmap> bitmaps, List<String> imagePaths) {
        if (selectedBitmaps == null || selectedBitmaps.isEmpty()) {
            Log.e("ImageSavingManager", "No images selected to delete.");
            return;
        }
        for (Bitmap bitmap : new ArrayList<>(selectedBitmaps)) {
            int index = bitmaps.indexOf(bitmap);
            if (index != -1) {
                bitmaps.remove(index);
                String path = imagePaths.get(index);
                File file = new File(path);
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("ImageSavingManager", "Deleted file: " + path);
                    } else {
                        Log.e("ImageSavingManager", "Failed to delete file: " + path);
                    }
                }
                imagePaths.remove(index);
            }
        }
        selectedBitmaps.clear();
    }

    public void saveImageToCharacterFolder(Context context, Bitmap bitmap, int characterId, String filename) {
        if (bitmap == null || filename == null) {
            Log.e("ImageSavingManager", "Invalid input for saving image to character folder");
            return;
        }

        String characterFolderName = characterMapping.getPaddedId(characterId);
        String directoryPath = Environment.DIRECTORY_PICTURES + "/TrainingImages/" + characterFolderName;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
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


    public void clearImageCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.isDirectory()) {
            for (File file : cacheDir.listFiles()) {
                if (file.getName().endsWith(".bmp")) {
                    if (file.delete()) {
                        Log.d("ImageSavingManager", "Deleted cache file: " + file.getPath());
                    } else {
                        Log.e("ImageSavingManager", "Failed to delete cache file: " + file.getPath());
                    }
                }
            }
        }
        Log.d("ImageSavingManager", "Cache cleared.");
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


