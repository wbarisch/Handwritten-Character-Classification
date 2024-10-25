package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

public class SupportSetItem {
    Bitmap bitmap;
    String labelId;



    SupportSetItem(Bitmap bmp, String labelId){
        this.bitmap = bmp;
        this.labelId = labelId;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getlabelId(){
        return labelId;
    }



}
