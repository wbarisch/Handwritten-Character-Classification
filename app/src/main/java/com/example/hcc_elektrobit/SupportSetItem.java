package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class SupportSetItem {
    Bitmap bitmap;
    int labelId;
    String label = "Test";


    SupportSetItem(Bitmap bmp, int labelId){
        this.bitmap = bmp;
        this.labelId = labelId;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public int getlabelId(){
        return labelId;
    }

    public String getlabel(){ return label; }

}
