package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    Bitmap bitmap;
    int pred;

    HistoryItem(Bitmap bmp, int prediction){
        this.bitmap = bmp;
        this.pred = prediction;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public int getPred(){
        return pred;
    }
}
