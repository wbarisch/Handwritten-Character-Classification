package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    String pred_tensor;
    Bitmap bitmap;
    int pred;

    HistoryItem(Bitmap bmp, int prediction, String prediction_tensor){
        this.bitmap = bmp;
        this.pred = prediction;
        this.pred_tensor = prediction_tensor;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public int getPred(){
        return pred;
    }
}
