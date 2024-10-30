package com.example.hcc_elektrobit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class SupportSetActivity extends AppCompatActivity {

    private GridView gridView;
    private SupportSetViewModel viewModel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menuButton);
        item.setTitle("Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuButton) {
            Intent intent = new Intent(SupportSetActivity.this, JMainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.updateSet();
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, viewModel.getItems());
        gridView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supportsetmain);

        viewModel = new ViewModelProvider(this).get(SupportSetViewModel.class);

        gridView = findViewById(R.id.grid_view);
        Button clearHistory = findViewById(R.id.clear_set);
        Button createImgButton = findViewById(R.id.draw_item);

        viewModel.updateSet();
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, viewModel.getItems());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SupportSetItem selectedItem = (SupportSetItem) parent.getItemAtPosition(position);
                showItemOptionsDialog(selectedItem);
            }
        });

        createImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SupportSetActivity.this, SupportSetDrawingActivity.class);
                startActivity(intent);
            }
        });

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clearSet();
                updateGridView();
            }
        });
    }

    private void showItemOptionsDialog(SupportSetItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Choose an action")
                .setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(item);
                    } else if (which == 1) {
                        Log.i("test", String.valueOf(viewModel.getItems().size()));
                        viewModel.removeItem(item);
                        updateGridView();
                        Toast.makeText(this, "Image deleted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showRenameDialog(SupportSetItem item) {
        AlertDialog.Builder renameDialog = new AlertDialog.Builder(this);
        renameDialog.setTitle("Rename Image");

        final EditText input = new EditText(this);
        input.setText(item.getLabelId());
        renameDialog.setView(input);

        renameDialog.setPositiveButton("Rename", (dialog, which) -> {
            String newLabel = input.getText().toString();
            if (!newLabel.isEmpty()) {
                viewModel.renameItem(item, newLabel);
                updateGridView();
                Toast.makeText(this, "Image renamed!", Toast.LENGTH_SHORT).show();
            }
        });

        renameDialog.setNegativeButton("Cancel", null);
        renameDialog.show();
    }

    private void updateGridView() {
        Log.i("test2", String.valueOf(viewModel.getItems().size()));
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, viewModel.getItems());
        gridView.setAdapter(adapter);
    }
}
