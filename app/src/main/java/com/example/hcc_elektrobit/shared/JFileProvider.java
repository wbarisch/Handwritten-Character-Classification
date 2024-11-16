package com.example.hcc_elektrobit.shared;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class JFileProvider extends FileProvider {

    // Singleton instance
    private static JFileProvider instance;

    public JFileProvider() {}

    // Method to get the single instance
    public static synchronized JFileProvider getInstance() {
        if (instance == null) {
            instance = new JFileProvider();
        }
        return instance;
    }

    // Internal method to get context
    private static Context getFileProviderContext() {
        return HCC_Application.getAppContext();
    }

    // Instance methods to access resources and directories
    public AssetFileDescriptor getAssetFileDescriptor(String searchString) throws IOException {
        AssetManager assetManager = getFileProviderContext().getAssets();
        return assetManager.openFd(searchString);
    }

    public File getCacheDir() {
        return getFileProviderContext().getCacheDir();
    }

    public File getInternalDir() {
        return getFileProviderContext().getFilesDir();
    }

    public File getExternalDir(String path) {
        return getFileProviderContext().getExternalFilesDir(path);
    }

    public ContentResolver getContentResolver() {
        return getFileProviderContext().getContentResolver();
    }

    public AssetManager getAssets() {
        return getFileProviderContext().getAssets();
    }
}
