package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    Bitmap bitmap;
    String pred;
    HistoryItem(Bitmap bmp, String prediction){
        this.bitmap = bmp;
        this.pred = prediction;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getPred(){
        return pred;
    }
}
