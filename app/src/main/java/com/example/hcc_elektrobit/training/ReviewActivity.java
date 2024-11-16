package com.example.hcc_elektrobit.training;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.utils.CharacterMapping;
import com.example.hcc_elektrobit.utils.DialogManager;
import com.example.hcc_elektrobit.utils.ImageSavingManager;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> imagePaths;
    private ArrayList<Bitmap> selectedBitmaps;
    private ReviewAdapter reviewAdapter;
    private GridView gridView;
    private CheckBox selectAllCheckBox;
    private ImageButton trashButton;
    private DialogManager dialogManager;
    private ImageSavingManager imageSavingManager;
    private CharacterMapping characterMapping;
    private int selectedCharacterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        dialogManager = new DialogManager(this);
        characterMapping = new CharacterMapping();
        imageSavingManager = new ImageSavingManager(null, characterMapping);

        gridView = findViewById(R.id.grid_view);
        Button keepButton = findViewById(R.id.keep_button);
        selectAllCheckBox = findViewById(R.id.select_all_checkbox);
        trashButton = findViewById(R.id.trash_button);

        Intent intent = getIntent();

        bitmaps = new ArrayList<>();
        selectedBitmaps = new ArrayList<>();

        selectedCharacterId = intent.getIntExtra("selectedCharacterId", -1);

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
                    imageSavingManager.saveSelectedImages(this, selectedBitmaps, selectedCharacterId);
                    imageSavingManager.clearImageCache(this);
                    selectedBitmaps.clear();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("operation", "keep_selected");
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }, () -> {});
            } else {
                Toast.makeText(this, "No images selected to keep.", Toast.LENGTH_SHORT).show();
            }
        });

        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                reviewAdapter.selectAll();
            } else {
                reviewAdapter.deselectAll();
            }
        });

        trashButton.setOnClickListener(v -> {
            if (selectedBitmaps.isEmpty()) {
                Toast.makeText(this, "No images selected to delete.", Toast.LENGTH_SHORT).show();
            } else {
                dialogManager.showDeleteSelectedDialog(() -> {
                    imageSavingManager.deleteSelectedImages(this, selectedBitmaps, bitmaps, imagePaths);
                    reviewAdapter.notifyDataSetChanged();
                    resetSelectAllCheckBox();
                    if (bitmaps.isEmpty()) {
                        Toast.makeText(this, "No images left.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, () -> {
                });
            }
        });
    }

    private void resetSelectAllCheckBox() {
        selectAllCheckBox.setOnCheckedChangeListener(null);
        selectAllCheckBox.setChecked(false);
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                reviewAdapter.selectAll();
            } else {
                reviewAdapter.deselectAll();
            }
        });
    }


}
