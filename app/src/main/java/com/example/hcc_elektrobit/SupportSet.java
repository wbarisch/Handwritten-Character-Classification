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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SupportSet {


    private static volatile SupportSet INSTANCE = null;


    private Set<SupportSetItem> SupportSetItems = new TreeSet<>(new Comparator<SupportSetItem>() {
        @Override
        public int compare(SupportSetItem o1, SupportSetItem o2) {
            int labelComparison = CharSequence.compare(o1.labelId, o2.labelId);
            if (labelComparison != 0) {
                return labelComparison;
            }


            labelComparison = Integer.compare(o1.getBitmap().getByteCount(), o2.getBitmap().getByteCount());
            if (labelComparison == 0) {
                return 1;
            }
            return labelComparison;
        }
    });
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
        String fileName = setItem.labelId + "_" + Integer.toString(setItem.bitmap.getGenerationId()) + ".png";
        setItem.setFileName(fileName);
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
                String labelId = file.getName().substring(0,file.getName().indexOf("_"));
                SupportSetItem _hi = new SupportSetItem(bmp, labelId);
                _hi.setFileName(file.getName());
                SupportSetItems.add(_hi);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void clearSet(Context context) {
        File bitmapDir = new File(context.getFilesDir(), "support_set");
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

    public void removeItem(SupportSetItem item, Context context) {
        SupportSetItems.remove(item);

        // Delete the corresponding file
        File fileDir = new File(context.getFilesDir(), "support_set");
        String fileName = item.getFileName();
        Log.i("File Name", fileName);
        File file = new File(fileDir, fileName);

        if (file.exists() && file.delete()) {
            Log.i("Image Deleted", "Deleted file: " + fileName);
        } else {
            Log.e("Image Deletion Failed", "Could not delete file: " + fileName);
        }
    }
}
