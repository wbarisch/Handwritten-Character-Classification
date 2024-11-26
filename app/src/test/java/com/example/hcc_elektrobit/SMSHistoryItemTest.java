package com.example.hcc_elektrobit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SMSHistoryItemTest {


    @Test
    public void testModel() {
        SMSHistoryItem shi = new SMSHistoryItem(null, "n", null);
        String model = shi.getModel();
        assertEquals("SMS", model);
    }

    @Test
    public void testPredId() {
        SMSHistoryItem shi = new SMSHistoryItem(null, "n", null);
        String predid = shi.getPred();
        assertEquals("n", predid);
    }

    @Test
    public void testSimMap() {
        Map<String, Float> simmapInput = new HashMap<>(1);
        SMSHistoryItem shi = new SMSHistoryItem(null, "n", simmapInput);
        Map<String, Float> simmap = shi.getOutputCollection();
        assertEquals(simmapInput, simmap);
    }

}
