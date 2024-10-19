package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

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


