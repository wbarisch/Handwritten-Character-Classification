package com.example.hcc_elektrobit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JMainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private Bitmap bitmap;
    private CNNonnxModel model;
    private DrawingCanvas drawingCanvas;
    private ImageView bitmapDisplay;
    private TextView recognizedCharTextView;
    private static final String TAG = "JMainActivity";
    boolean noActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmain);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        recognizedCharTextView = findViewById(R.id.recognized_char);
        bitmapDisplay = findViewById(R.id.bitmap_display);

        Button shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> showShareOrSaveDialog());

        model = new CNNonnxModel(this);
        noActivity = true;


        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                                if (out != null) {
                                    saveBitmapAsBMP(bitmap, out);
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


        drawingCanvas.setOnTouchListener((v, event) -> {
            noActivity = false;
            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                setTimeOut();
            }

            return true;
        });
    }


    private void showShareOrSaveDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");
        String[] options = {"Share", "Save to Device"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                shareImage(bitmap);
            } else if (which == 1) {
                saveImageUsingDocumentIntent();
            }
        });

        builder.show();
    }

    private void setTimeOut() {

        noActivity = true;

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted during timeout", e);
            }

            if (noActivity) {
                classifyCharacter();
            }
        }).start();
    }

    // Invoke external CharacterClassifier class from here to start processing the drawing.
    private void classifyCharacter(){

        bitmap = drawingCanvas.getBitmap(); // ! The return value invalid currently

        // TO DO:
        // - Call CharacterClassifier class
        // - To display the output character, set it to "recognizedCharTextView".

        int result = model.classifyAndReturnDigit(bitmap);

        bitmap = createBitmapFromFloatArray(model.preprocessBitmap(bitmap), 28, 28);

        runOnUiThread(() -> {

            recognizedCharTextView.setText(String.valueOf(result));

            //Display the image for testing
            bitmapDisplay.setImageBitmap(bitmap);


        });

        drawingCanvas.clearCanvas();

    }

    public Bitmap createBitmapFromFloatArray(float[] floatArray, int width, int height) {

        if (floatArray.length != width * height) {
            throw new IllegalArgumentException("Float array length must match width * height");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];

        for (int i = 0; i < floatArray.length; i++) {

            float value = floatArray[i];

            value = Math.max(0, Math.min(1, value));

            int grayscale = (int) (value * 255);
            int color = Color.argb(255, grayscale, grayscale, grayscale);

            pixels[i] = color;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    private void saveBitmapAsBMP(Bitmap bitmap, OutputStream out) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int paddedRowSize = (width * 3 + 3) & (~3);
        int bmpSize = paddedRowSize * height;

        out.write(0x42); out.write(0x4D);
        out.write(intToByteArray(14 + 40 + bmpSize));
        out.write(new byte[4]);
        out.write(intToByteArray(14 + 40));
        out.write(intToByteArray(40));
        out.write(intToByteArray(width));
        out.write(intToByteArray(height));
        out.write(0x01); out.write(0x00);
        out.write(0x18); out.write(0x00);
        out.write(new byte[4]);
        out.write(intToByteArray(bmpSize));
        out.write(new byte[4]);
        out.write(new byte[4]);
        out.write(new byte[4]);
        out.write(new byte[4]);

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

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    private void shareImage(Bitmap bitmap) {

        if (bitmap == null) {
            return;
        }

        try {

            File dir = new File(getFilesDir(), "BitmapImages");

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
                saveBitmapAsBMP(bitmap, out);
                Log.d("ShareImage", "Image saved to: " + imageFile.getAbsolutePath());
            }

            Uri contentUri = FileProvider.getUriForFile(this, "com.example.hcc_elektrobit.fileprovider", imageFile);

            if (contentUri != null) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/bmp");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share image via"));
            }

            Log.d("ShareImage", "Image saved at: " + imageFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("ShareImage", "Failed to save the image.", e);
        }
    }

    private void saveImageUsingDocumentIntent() {
        if (bitmap == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/bmp");
        intent.putExtra(Intent.EXTRA_TITLE, "image_" + System.currentTimeMillis() + ".bmp");

        createDocumentLauncher.launch(intent);
    }

}
