package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.hcc_elektrobit.model.SMSComaparison;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(AndroidJUnit4.class)
public class CosineSimilarityTest {

    SMSComaparison SmsOnnxModel = SMSComaparison.getInstance();

    @Test
    public void testCosineSimilarity(){
        Bitmap bitmap = Bitmap.createBitmap(105, 105, Bitmap.Config.ARGB_8888);
        float sim = SmsOnnxModel.computeCosineSimilarity(new float[]{0.1f}, new float[]{0.1f});
        Assert.assertEquals(sim, 1, 0.0001);
    }
}