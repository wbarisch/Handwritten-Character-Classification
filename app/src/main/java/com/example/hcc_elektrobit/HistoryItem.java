package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    Object model_tensor;
    Bitmap bitmap;
    String pred;
    long timeCreated;

    HistoryItem(Bitmap bmp, String prediction, Object prediction_tensor){
        this.bitmap = bmp;
        this.pred = prediction;
        this.model_tensor = prediction_tensor;
        this.timeCreated = System.currentTimeMillis();
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public Object getPred(){
        return pred;
    }

    public Object getOutputCollection(){
        return model_tensor;
    }

    public String getModel(){
        return null;
    }

    public long getCreationTime() {
        return timeCreated;
    }
}