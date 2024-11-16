package com.example.hcc_elektrobit.history;

import android.graphics.Bitmap;

public class CNNHistoryItem extends HistoryItem{

    float[][] outputTensor;
    String predictionID;
    CNNHistoryItem(Bitmap bmp, String prediction, float[][] prediction_tensor) {

        super(bmp, prediction, prediction_tensor);
        outputTensor = prediction_tensor;
        predictionID = prediction;
    }

    @Override
    public String getModel() {
        return "CNN";
    }

    @Override
    public String getPred() {
        return predictionID;
    }

    @Override
    public float[][] getOutputCollection() {
        return outputTensor;
    }
}
