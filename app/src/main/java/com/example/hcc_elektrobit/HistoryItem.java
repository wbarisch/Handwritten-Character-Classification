package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    float[][] pred_tensor;
    Bitmap bitmap;
    int pred;

    HistoryItem(Bitmap bmp, int prediction, float[][] prediction_tensor){
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
