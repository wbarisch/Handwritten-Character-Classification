package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

import java.util.Objects;

import ai.onnxruntime.OnnxTensor;

public class SupportSetItem {
    Bitmap bitmap;
    String labelId;
    String fileName;
    float[] embeddingValues;

    SupportSetItem(Bitmap bmp, String labelId) {
        this.bitmap = bmp;
        this.labelId = labelId;
        float[][] emb = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bmp);
        embeddingValues = emb[0];
    }

    public float[] getEmbeddingValues() {
        return embeddingValues;
    }

    public void setEmbeddingValues(float[] embeddingValues) {
        this.embeddingValues = embeddingValues;
    }


    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getLabelId(){
        return labelId;
    }

    public void setLabelId(String newLabelId) {
        this.labelId = newLabelId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }






}