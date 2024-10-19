package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class History {

    private static volatile History INSTANCE = null;


    private List<HistoryItem> historyItems = new ArrayList<>();
    private History() {

    }

    public static History getInstance() {
        if(INSTANCE == null) {
            synchronized (History.class) {
                if (INSTANCE == null) {
                    INSTANCE = new History();
                }
            }
        }
        return INSTANCE;
    }

    public void addItem(HistoryItem _hi){
        historyItems.add(_hi);
    }

    public List<HistoryItem> getItems(){
        return historyItems;
    }

    /***
     *
     * @param historyItem
     * @param context
     *
     * Stores historyItem in internal storage as a lossless png under the name
     * "prediction"+random integer for uniqueness
     */
    public void saveItem(HistoryItem historyItem, Context context){
        File file = new File(context.getFilesDir(), "saved_bitmaps");
        if(!file.exists()){
            file.mkdir();
        }
        String randomFileSuffix = (int) (Math.random() * 1000000) + "";
        String fileName = historyItem.pred + randomFileSuffix + ".png";
        file = new File(file, fileName);
        try(FileOutputStream out = new FileOutputStream(file)){
            historyItem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("Bitmap Saved!", "Bitmap save in " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
