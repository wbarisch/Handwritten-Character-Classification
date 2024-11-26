package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CNNonnxModelTest {

    private static CNNonnxModel cnnOnnxModel;
    private static Bitmap testBitmap;

    @BeforeClass
    public static void setUp() throws IOException {
        Context context = ApplicationProvider.getApplicationContext();
        cnnOnnxModel = CNNonnxModel.getInstance(context);

        try (InputStream inputStream = context.getAssets().open("number6.bmp")) {
            testBitmap = BitmapFactory.decodeStream(inputStream);
        }
    }

    @AfterClass
    public static void tearDown() {
        cnnOnnxModel.close();
    }

    @Test
    public void testModelInitialization() {
        assertNotNull("Model should be initialized", cnnOnnxModel);
    }

    @Test
    public void testClassify() {
        float[][] output = cnnOnnxModel.classify(testBitmap);
        assertNotNull("Output tensor should not be null", output);
        assertTrue("Output tensor should contain values", output.length > 0 && output[0].length > 0);
    }

    @Test
    public void testClassifyAndReturnDigit() {
        int digit = cnnOnnxModel.classifyAndReturnDigit(testBitmap);
        assertTrue("Predicted digit should be between 0 and 9", digit >= 0 && digit <= 9);
    }

    @Test
    public void testClassifyAndReturnIntAndTensor() {
        Pair<Integer, float[][]> result = cnnOnnxModel.classifyAndReturnIntAndTensor(testBitmap);
        assertNotNull("Result Pair should not be null", result);
        assertNotNull("Tensor in result should not be null", result.second);
        assertTrue("Output tensor should contain values", result.second.length > 0 && result.second[0].length > 0);
        assertTrue("Predicted digit should be between 0 and 9", result.first >= 0 && result.first <= 9);
    }

    @Test
    public void testPreprocessBitmap() {
        float[] processedData = cnnOnnxModel.preprocessBitmap(testBitmap);
        assertNotNull("Processed data should not be null", processedData);
        assertEquals("Processed data should have 784 values (28x28)", 784, processedData.length);
        for (float value : processedData) {
            assertTrue("Pixel values should be between 0 and 1", value >= 0.0f && value <= 1.0f);
        }
    }
}
