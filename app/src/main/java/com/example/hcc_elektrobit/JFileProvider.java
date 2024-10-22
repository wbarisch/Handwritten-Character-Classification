package com.example.hcc_elektrobit;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import androidx.core.content.FileProvider;
import java.io.IOException;

public class JFileProvider extends FileProvider {
    private static Context getFileProviderContext(){
        return HCC_Application.getAppContext();
    }

    protected static AssetFileDescriptor getAssetFileDescriptor(String searchString) throws IOException {
        AssetManager assetManager = getFileProviderContext().getAssets();
        return assetManager.openFd(searchString);
    }
}
