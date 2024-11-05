package com.example.hcc_elektrobit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class JHistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private String path;
    private ActivityResultLauncher<Uri> directoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jhistory);
        History.getInstance().updateHistory(HCC_Application.getAppContext());

        viewModel = new HistoryViewModel();

        setupGridView();
        setupButtons();
        setupMenu();

        directoryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                result -> {
                    if (result != null) {
                        viewModel.export(path, result);
                    }
                }
        );

        // Observe history items from ViewModel
        viewModel.getHistoryItems().observe(this, this::updateAdapter);
    }

    private void setupGridView() {
        GridView gridView = findViewById(R.id.grid_view);
        adapter = new HistoryAdapter(this, R.layout.history_item, viewModel.getHistoryItems().getValue());
        gridView.setAdapter(adapter);

    }

    private void updateAdapter(List<HistoryItem> items) {
        adapter.clear();
        if (items != null) {
            adapter.addAll(items);
        }
        adapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        Button clearHistory = findViewById(R.id.clear_history);
        Button exportHistory = findViewById(R.id.export_history);
        Button goToEval = findViewById(R.id.eval_button_act);

        clearHistory.setOnClickListener(v -> viewModel.clearHistory());
        exportHistory.setOnClickListener(v -> chooseDestinationDirectory());
        goToEval.setOnClickListener(v -> goToModelEval());

    }

    private void chooseDestinationDirectory() {
        directoryLauncher.launch(null);
    }

    private void setupMenu() {
        File source = new File(getFilesDir(), "saved_bitmaps");
        path = source.toString();
    }

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
            Intent intent = new Intent(JHistoryActivity.this, JMainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToModelEval(){
        Intent modeEvalIntent = new Intent(this, JEvaluationActivity.class);
        startActivity(modeEvalIntent);
    }


}
