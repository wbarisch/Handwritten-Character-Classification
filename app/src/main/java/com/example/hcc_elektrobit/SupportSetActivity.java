package com.example.hcc_elektrobit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SupportSetActivity extends AppCompatActivity {

    GridView gridView;

    @Override
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
            Intent intent = new Intent(SupportSetActivity.this, JMainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    protected void onResume() {
        super.onResume();
        SupportSet.getInstance().updateSet(this);
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, SupportSet.getInstance().getItems());
        gridView.setAdapter(adapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supportsetmain);

        gridView = findViewById(R.id.grid_view);
        Button clearHistory = findViewById(R.id.clear_set);
        Button createImgButton = findViewById(R.id.draw_item);
        SupportSet.getInstance().updateSet(this);
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, SupportSet.getInstance().getItems());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SupportSetItem selectedItem = (SupportSetItem) parent.getItemAtPosition(position);

                // Show dialog to choose between Rename or Delete
                AlertDialog.Builder builder = new AlertDialog.Builder(SupportSetActivity.this);
                builder.setTitle("Choose an action");
                builder.setItems(new CharSequence[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Rename option selected
                            showRenameDialog(selectedItem);
                        } else if (which == 1) {
                            // Delete option selected
                            deleteImage(selectedItem);
                        }
                    }
                });
                builder.show();
            }
        });

        createImgButton.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SupportSetActivity.this, SupportSetDrawingActivity.class);
                startActivity(intent);
            }
        });

        clearHistory.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the clearHistory method from the History class
                SupportSet.getInstance().clearSet(SupportSetActivity.this);
                SupportSet.getInstance().updateSet(SupportSetActivity.this);
                SupportSetAdapter adapter = new SupportSetAdapter( SupportSetActivity.this, R.layout.support_set_item, SupportSet.getInstance().getItems());
                gridView.setAdapter(adapter);
            }
        });

    }

    private void showRenameDialog(SupportSetItem item) {
        AlertDialog.Builder renameDialog = new AlertDialog.Builder(SupportSetActivity.this);
        renameDialog.setTitle("Rename Image");

        final EditText input = new EditText(SupportSetActivity.this);
        input.setText(item.getlabelId());  // Show current label as default
        renameDialog.setView(input);

        renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newLabel = input.getText().toString();
                if (!newLabel.isEmpty()) {
                    SupportSetItem temp = new SupportSetItem(item.getBitmap(),newLabel);
                    temp.setLabelId(newLabel);  // Update the label
                    SupportSet.getInstance().saveItem(temp, SupportSetActivity.this); // Save the renamed item
                    deleteImage(item);
                    SupportSet.getInstance().updateSet(SupportSetActivity.this);  // Refresh data
                    gridView.invalidateViews();  // Refresh the GridView to show the new label
                    Toast.makeText(SupportSetActivity.this, "Image renamed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        renameDialog.setNegativeButton("Cancel", null);
        renameDialog.show();
    }

    private void deleteImage(SupportSetItem item) {
        // Delete from SupportSet and remove the file
        SupportSet.getInstance().removeItem(item, SupportSetActivity.this);
        SupportSet.getInstance().updateSet(SupportSetActivity.this);  // Update the set after deletion
        gridView.invalidateViews();  // Refresh the GridView
        SupportSet.getInstance().updateSet(SupportSetActivity.this);
        SupportSetAdapter adapter = new SupportSetAdapter( SupportSetActivity.this, R.layout.support_set_item, SupportSet.getInstance().getItems());
        gridView.setAdapter(adapter);
        Toast.makeText(SupportSetActivity.this, "Image deleted!", Toast.LENGTH_SHORT).show();
    }


}
