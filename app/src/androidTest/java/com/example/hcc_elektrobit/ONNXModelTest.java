package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.hcc_elektrobit.model.SMSonnxModel;
import com.example.hcc_elektrobit.shared.HCC_Application;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(AndroidJUnit4.class)
public class ONNXModelTest {

    Context context = HCC_Application.getAppContext();
    SMSonnxModel SmsOnnxModel = SMSonnxModel.getInstance(context);

    @Test
    public void testModeInputOutputNames(){
        Bitmap bitmap = Bitmap.createBitmap(SmsOnnxModel.bmp_size, SmsOnnxModel.bmp_size, Bitmap.Config.ARGB_8888);
        float sim = SmsOnnxModel.classify_similarity(bitmap, bitmap);
        Assert.assertEquals(sim, SmsOnnxModel.get_same_image_similarity(), 0.0001);
    }
}