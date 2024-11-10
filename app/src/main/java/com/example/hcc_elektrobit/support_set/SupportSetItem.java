package com.example.hcc_elektrobit.support_set;

import android.graphics.Bitmap;

import com.example.hcc_elektrobit.model.SMSComaparisonOnnxModel;
import com.example.hcc_elektrobit.model.SMSEmbeddingOnnxModel;

import ai.onnxruntime.OnnxTensor;

public class SupportSetItem {
    Bitmap bitmap;
    String labelId;
    String fileName;
    OnnxTensor imgEmbedding;



    SupportSetItem(Bitmap bmp, String labelId){
        this.bitmap = bmp;
        this.labelId = labelId;
        float[][] emb = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bmp);
        imgEmbedding = SMSComaparisonOnnxModel.getInstance().loadTensor(emb);

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

    public OnnxTensor getImgEmbedding() {
        return imgEmbedding;
    }

    public void setImgEmbedding(OnnxTensor imgEmbedding) {
        this.imgEmbedding = imgEmbedding;
    }




}
