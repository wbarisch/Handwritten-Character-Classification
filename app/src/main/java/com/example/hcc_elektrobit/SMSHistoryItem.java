package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

import java.util.Map;

public class SMSHistoryItem extends HistoryItem{

    Map<String,Float> simMap;
    String predictionID;

    SMSHistoryItem(Bitmap bmp, String prediction, Map<String, Float> similarityMap) {
        super(bmp, prediction, similarityMap);
        simMap = similarityMap;
        predictionID = prediction;

    }

    @Override
    public Map<String, Float> getOutputCollection() {
        return simMap;
    }

    @Override
    public String getPred() {
        return predictionID;
    }

    @Override
    public String getModel() {
        return "SMS";
    }
}
