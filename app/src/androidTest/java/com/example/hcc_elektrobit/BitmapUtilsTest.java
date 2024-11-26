package com.example.hcc_elektrobit;

import android.graphics.Bitmap;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.hcc_elektrobit.utils.BitmapUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)

public class BitmapUtilsTest {

    @Test
    public void testBitmapCenteringAndResizing() {
        Bitmap bitmapBefore = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Bitmap bitmapAfter = BitmapUtils.centerAndResizeBitmap(bitmapBefore, 30,false);

        assert(bitmapAfter.getHeight() == 30);
        assert(bitmapAfter.getWidth() == 30);
    }

}
