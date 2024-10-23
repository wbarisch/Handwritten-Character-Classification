package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SupportSet {


    private static volatile SupportSet INSTANCE = null;


    private Set<SupportSetItem> SupportSetItems = new HashSet<>();
    private SupportSet() {
    }

    public static SupportSet getInstance() {
        if(INSTANCE == null) {
            synchronized (SupportSet.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SupportSet();
                }
            }
        }
        return INSTANCE;
    }

    public void addItem(SupportSetItem _hi){
        SupportSetItems.add(_hi);
    }

    public List<SupportSetItem> getItems(){
        return new ArrayList<>(SupportSetItems);
    }

    public void saveItem(SupportSetItem setItem, Context context){
        File file = new File(context.getFilesDir(), "support_set");
        if(!file.exists()){
            file.mkdir();
        }
        String fileName = setItem.labelId + "_" +Integer.toString(setItem.bitmap.getGenerationId()) + ".png";
        file = new File(file, fileName);
        try(FileOutputStream out = new FileOutputStream(file)){
            setItem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("Bitmap Saved!", "Bitmap save in " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // fill the history set with the saved imgs
    public void updateSet(Context context){
        File bitmapDir = new File(context.getFilesDir(), "support_set");
        if(!bitmapDir.exists()){
            return;
        }

        SupportSetItems.clear();
        for (File file: Objects.requireNonNull(bitmapDir.listFiles())) {
            try(FileInputStream in = new FileInputStream(file)){
                Bitmap bmp = BitmapFactory.decodeStream(in);
                int labelId = Integer.parseInt(file.getName().substring(0,file.getName().indexOf("_")));
                SupportSetItem _hi = new SupportSetItem(bmp, labelId);
                SupportSetItems.add(_hi);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void clearSet(Context context) {
        File bitmapDir = new File(context.getFilesDir(), "saved_bitmaps");
        if (bitmapDir.exists()) {
            for (File file : Objects.requireNonNull(bitmapDir.listFiles())) {
                if (file.delete()) {
                    Log.i("File Deleted", "Deleted file: " + file.getName());
                } else {
                    Log.e("File Deletion Failed", "Failed to delete file: " + file.getName());
                }
            }
            SupportSetItems.clear();
            Log.i("History Cleared", "All history items have been deleted and memory cleared.");
        } else {
            Log.i("No History Found", "No history to delete.");
        }
    }
}