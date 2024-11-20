
package com.example.hcc_elektrobit.history;

import android.net.Uri;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hcc_elektrobit.shared.HCC_Application;
import com.example.hcc_elektrobit.shared.JFileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private final MutableLiveData<List<HistoryItem>> historyItems = new MutableLiveData<>(History.getInstance().getItems());

    public HistoryViewModel() {
        loadHistory();
    }

    public LiveData<List<HistoryItem>> getHistoryItems() {
        return historyItems;
    }

    public void loadHistory() {

        List<HistoryItem> items = History.getInstance().getItems();
        Log.e("actual items", items.toString());
        historyItems.postValue(items);
    }

    public void clearHistory() {
        History.getInstance().clearHistory();
        loadHistory();
    }

    public void export(String path, Uri result){
        copyFolderToUri(path, result);
    }

    private void copyFolderToUri(String sourcePath, Uri destinationUri) {
        try {
            File sourceFolder = new File(sourcePath);
            DocumentFile destinationDir = DocumentFile.fromTreeUri(HCC_Application.getAppContext(), destinationUri);
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
                     OutputStream outStream = JFileProvider.getInstance().getContentResolver().openOutputStream(newFile.getUri())) {
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
