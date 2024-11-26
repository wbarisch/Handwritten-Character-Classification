package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.hcc_elektrobit.history.HistoryItem;

public class HistoryItemTest {

    private Bitmap testBitmap;
    private String testPrediction;
    private Object testTensor;
    private HistoryItem historyItem;

    @Before
    public void setUp() {
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        testPrediction = "TestPrediction";
        testTensor = new float[][]{{1.0f, 2.0f}, {3.0f, 4.0f}};
        historyItem = new HistoryItem(testBitmap, testPrediction, testTensor);
    }

    @Test
    public void testGetBitmap() {
        assertEquals(testBitmap, historyItem.getBitmap());
    }

    @Test
    public void testGetPred() {
        assertEquals(testPrediction, historyItem.getPred());
    }

    @Test
    public void testGetOutputCollection() {
        assertEquals(testTensor, historyItem.getOutputCollection());
    }

    @Test
    public void testGetModel() {
        assertNull(historyItem.getModel());
    }
}
