package com.example.hcc_elektrobit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;

public class JFileProvider extends FileProvider {
    private static Context getFileProviderContext(){
        return HCC_Application.getAppContext();
    }

    protected static AssetFileDescriptor getAssetFileDescriptor(String searchString) throws IOException {
        AssetManager assetManager = getFileProviderContext().getAssets();
        return assetManager.openFd(searchString);
    }

    protected static File getCacheDir(){
        return getFileProviderContext().getCacheDir();
    }

    protected static File getInternalDir(){
        return getFileProviderContext().getFilesDir();
    }

    protected static File getExternalDir(String _path){
        return getFileProviderContext().getExternalFilesDir(_path);
    }

    protected static ContentResolver getContentResolver(){
        return getFileProviderContext().getContentResolver();
    }

    protected static AssetManager getAssets(){

        return getFileProviderContext().getAssets();
    }

}
