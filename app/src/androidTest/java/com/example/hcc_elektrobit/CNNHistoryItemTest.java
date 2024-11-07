package com.example.hcc_elektrobit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class CNNHistoryItemTest {

    private Bitmap testBitmap;
    private String predictionID = "testPrediction";
    private float[][] outputTensor = {
            {0.1f, 0.2f, 0.3f},
            {0.4f, 0.5f, 0.6f}
    };
    private CNNHistoryItem cnnHistoryItem;

    @Before
    public void setUp() {
        cnnHistoryItem = new CNNHistoryItem(testBitmap, predictionID, outputTensor);
    }

    @Test
    public void testGetModel() {
        assertEquals("Model should be CNN", "CNN", cnnHistoryItem.getModel());
    }

    @Test
    public void testGetPred() {
        assertEquals("Prediction ID should match", predictionID, cnnHistoryItem.getPred());
    }

    @Test
    public void testGetOutputCollection() {
        assertArrayEquals("Output tensor should match", outputTensor, cnnHistoryItem.getOutputCollection());
    }

}
