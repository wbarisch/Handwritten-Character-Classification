package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class SupportSetItem {
    Bitmap bitmap;
    String labelId;
    String fileName;



    SupportSetItem(Bitmap bmp, String labelId){
        this.bitmap = bmp;
        this.labelId = labelId;
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
