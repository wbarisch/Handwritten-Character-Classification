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

public class History {


    private static volatile History INSTANCE = null;


    private Set<HistoryItem> historyItems = new HashSet<>();
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
        return new ArrayList<>(historyItems);
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
        String fileName = historyItem.pred + Integer.toString(historyItem.bitmap.getGenerationId()) + ".png";
        file = new File(file, fileName);
        try(FileOutputStream out = new FileOutputStream(file)){
            historyItem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("Bitmap Saved!", "Bitmap save in " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // fill the history set with the saved imgs
    public void updateHistory(Context context){
        File bitmapDir = new File(context.getFilesDir(), "saved_bitmaps");
        if(!bitmapDir.exists()){
            return;
        }
        historyItems.clear();
        for (File file: Objects.requireNonNull(bitmapDir.listFiles())) {
            try(FileInputStream in = new FileInputStream(file)){
                Bitmap bmp = BitmapFactory.decodeStream(in);
                int pred = Integer.parseInt(file.getName().charAt(0)+"");
                HistoryItem _hi = new HistoryItem(bmp, pred);
                historyItems.add(_hi);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
