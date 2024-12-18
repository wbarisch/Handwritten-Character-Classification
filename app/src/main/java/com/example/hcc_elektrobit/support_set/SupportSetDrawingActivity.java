package com.example.hcc_elektrobit.support_set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hcc_elektrobit.shared.DrawingCanvas;
import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;

import java.util.Objects;

public class SupportSetDrawingActivity extends AppCompatActivity implements TimeoutActivity {

    private DrawingCanvas drawingCanvas;
    private TextView labelTextView;
    private ImageView bitmapDisplay;

    private Bitmap bitmap;

    private String labelId = "";
    private int bitmapSize = 105;
    Timer canvasTimer;
    boolean timerStarted = false;

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menuButton);
        item.setTitle("Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.menuButton) {
            Intent intent = new Intent(SupportSetDrawingActivity.this, SupportSetActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supporsetdrawing);

        drawingCanvas = findViewById(R.id.drawing_canvas);
        labelTextView = findViewById(R.id.label);
        bitmapDisplay = findViewById(R.id.bitmap_display);
        Button optionsButton = findViewById(R.id.options_button);

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        drawingCanvas.setOnTouchListener((v, event) -> {
            if (timerStarted) {
                canvasTimer.cancel();
                timerStarted = false;
            }

            drawingCanvas.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                bitmap = drawingCanvas.getBitmap(bitmapSize, true, 3f);
                canvasTimer = new Timer(this, 1000);
                new Thread(canvasTimer).start();
                timerStarted = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }

            return true;
        });
        showOptionsDialog();
    }

    private void showOptionsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_dialog, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SupportSetDrawingActivity.this);
        dialogBuilder.setView(dialogView);

        final EditText imageIdInput = dialogView.findViewById(R.id.image_id);
        final EditText bitmapSizeInput = dialogView.findViewById(R.id.bitmap_size);

        bitmapSizeInput.setText(String.valueOf(bitmapSize));

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String imageId = imageIdInput.getText().toString();
                String bitmapSizeStr = bitmapSizeInput.getText().toString();

                if (!imageId.isEmpty()) {
                    labelId = imageId;
                }

                if (!bitmapSizeStr.isEmpty()) {
                    bitmapSize = Integer.parseInt(bitmapSizeStr);
                }

                labelTextView.setText(String.valueOf(labelId));

                Toast.makeText(SupportSetDrawingActivity.this, "Image ID: " + imageId + "\nBitmap Size: " + bitmapSize, Toast.LENGTH_SHORT).show();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public void onTimeout(){

        saveItem();
        drawingCanvas.clear();
        timerStarted = false;

    }

    private void saveItem(){

        if (Objects.equals(labelId, "")) {
            runOnUiThread(() -> {

                Toast.makeText(this, "Please set the label Id before drawing", Toast.LENGTH_SHORT).show();

            });
            return;
        }

        bitmap = drawingCanvas.getBitmap(bitmapSize, true, 3f);

        if (bitmap == null) {
            Log.e("JMainActivity", "Bitmap is null in classifyCharacter");
            return;
        }



        SupportSet supportSet = SupportSet.getInstance();
        SupportSetItem supportSetItem = new SupportSetItem(bitmap, labelId);

        supportSet.saveItem(supportSetItem);


        runOnUiThread(() -> {

            bitmapDisplay.setImageBitmap(bitmap);

        });
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}