package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class HistoryItem {
    Object model_tensor;
    Bitmap bitmap;
    String pred;

    HistoryItem(Bitmap bmp, String prediction, Object prediction_tensor){
        this.bitmap = bmp;
        this.pred = prediction;
        this.model_tensor = prediction_tensor;
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
}
