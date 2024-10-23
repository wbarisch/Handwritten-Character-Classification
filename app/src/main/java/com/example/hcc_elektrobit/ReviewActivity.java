package com.example.hcc_elektrobit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> imagePaths;
    private ArrayList<Bitmap> selectedBitmaps;
    private ReviewAdapter reviewAdapter;
    private GridView gridView;

    private DialogManager dialogManager;
    private ImageSavingManager imageSavingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        dialogManager = new DialogManager(this);
        imageSavingManager = new ImageSavingManager(null);

        gridView = findViewById(R.id.grid_view);
        Button keepButton = findViewById(R.id.keep_button);
        Button discardButton = findViewById(R.id.discard_button);

        Intent intent = getIntent();

        bitmaps = new ArrayList<>();
        selectedBitmaps = new ArrayList<>();

        String selectedCharacter = intent.getStringExtra("selectedCharacter");

        if (intent != null) {
            imagePaths = intent.getStringArrayListExtra("image_paths");
            if (imagePaths != null) {
                for (String path : imagePaths) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    }
                }
            }
        }

        reviewAdapter = new ReviewAdapter(this, bitmaps, selectedBitmaps);
        gridView.setAdapter(reviewAdapter);

        keepButton.setOnClickListener(v -> {
            if (!selectedBitmaps.isEmpty()) {
                dialogManager.showKeepSelectedDialog(() -> {
                    ArrayList<String> selectedPaths = new ArrayList<>();
                    for (Bitmap selectedBitmap : selectedBitmaps) {
                        int index = bitmaps.indexOf(selectedBitmap);
                        if (index >= 0 && index < imagePaths.size()) {
                            String path = imagePaths.get(index);
                            if (!selectedPaths.contains(path)) {
                                selectedPaths.add(path);
                            }
                        }
                    }

                    for (String path : selectedPaths) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        String character = selectedCharacter;
                        imageSavingManager.saveImageToCharacterFolder(this, bitmap, character, "image_" + System.currentTimeMillis() + ".bmp");
                    }

                    selectedBitmaps.clear();

                    Intent resultIntent = new Intent();
                    resultIntent.putStringArrayListExtra("selected_paths", selectedPaths);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }, () -> {});
            }
        });

        discardButton.setOnClickListener(v -> {
            dialogManager.showDiscardAllDialog(() -> {
                imageSavingManager.deleteAllImages(this);
                finish();
            }, () -> {
            });
        });
    }
}
