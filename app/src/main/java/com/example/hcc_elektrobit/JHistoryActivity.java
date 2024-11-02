package com.example.hcc_elektrobit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JHistoryActivity extends AppCompatActivity {

    private String path;
    private ActivityResultLauncher<Uri> directoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jhistory);

        directoryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            copyFolderToUri(path, result);
                        }
                    }
                });


        setupMenu();
        setupButtons();
    }

    private void setupMenu() {

        File source = new File(getFilesDir(), "saved_bitmaps");
        path = source.toString();
    }

    private void setupButtons() {
        GridView gridView = findViewById(R.id.grid_view);
        Button clearHistory = findViewById(R.id.clear_history);
        Button exportHistory = findViewById(R.id.export_history);

        History.getInstance().updateHistory(this);
        HistoryAdapter adapter = new HistoryAdapter(this, R.layout.history_item, History.getInstance().getItems());
        gridView.setAdapter(adapter);

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History.getInstance().clearHistory(JHistoryActivity.this);
                History.getInstance().updateHistory(JHistoryActivity.this);
                HistoryAdapter newAdapter = new HistoryAdapter(JHistoryActivity.this, R.layout.history_item, History.getInstance().getItems());
                gridView.setAdapter(newAdapter);
            }
        });

        exportHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDestinationDirectory();
            }
        });
    }

    private void chooseDestinationDirectory() {
        directoryLauncher.launch(null);
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
        int id = item.getItemId();
        if (id == R.id.menuButton) {
            Intent intent = new Intent(JHistoryActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyFolderToUri(String sourcePath, Uri destinationUri) {
        try {
            File sourceFolder = new File(sourcePath);
            DocumentFile destinationDir = DocumentFile.fromTreeUri(this, destinationUri);
            copyFilesRecursively(sourceFolder, destinationDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFilesRecursively(File source, DocumentFile destinationDir) {
        if (source.isDirectory()) {
            DocumentFile newDir = destinationDir.findFile(source.getName());
            if (newDir == null) {
                newDir = destinationDir.createDirectory(source.getName());
            }
            for (File file : source.listFiles()) {
                copyFilesRecursively(file, newDir);
            }
        } else {
            try {
                DocumentFile newFile = destinationDir.findFile(source.getName());
                if (newFile == null) {
                    newFile = destinationDir.createFile("application/octet-stream", source.getName());
                }
                try (InputStream inStream = new FileInputStream(source);
                     OutputStream outStream = getContentResolver().openOutputStream(newFile.getUri())) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, length);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
