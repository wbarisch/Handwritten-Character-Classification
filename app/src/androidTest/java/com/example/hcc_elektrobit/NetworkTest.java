package com.example.hcc_elektrobit;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.*;

import com.example.hcc_elektrobit.model.SMSComaparisonOnnxModel;
import com.example.hcc_elektrobit.shared.HCC_Application;
import com.example.hcc_elektrobit.support_set.SupportSetItem;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class NetworkTest {

    // Context for the target application being instrumented
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    // The neural network model
    private static final SMSComaparisonOnnxModel model = SMSComaparisonOnnxModel.getInstance();

    // Use the app's SupportSet items as a sample data
    private final List<SupportSetItem> sampleData = HCC_Application.getSupportSet().getItems();

    // Test the setup
    @Test
    public void testSetUp(){

        Log.d("NetworkTest", "Testing the setup");

        assertNotNull("App context is null.", context);
        assertNotNull("Model is null.", model);
        assertNotNull("Sample data is null.", sampleData);

        //if(sampleData != null) {

        assertFalse("No sample found.", sampleData.isEmpty());

        Log.d("NetworkTest", "Sample data size: " + sampleData.size());

        //}

    }

    // Test non-quantized model
    @Test
    public void testNotQuantized() {

        if(sampleData.isEmpty()){

            fail("No sample to test!");

        } else {
            model.setQuantized(false);

            for (SupportSetItem item : sampleData) {

                String labelId = item.getLabelId();
                String result = model.classifyAndReturnPredAndSimilarityMap(item.getBitmap()).first;

                Log.d("testNotQuantized", "Testing a sample of character: " + labelId);
                Log.d("testNotQuantized", "Expected: " + labelId + ", Found: " + result);

                assertEquals("Testing a sample of character \"" + labelId + "\" failed.", labelId, result);

            }
        }

    }

    // Test quantized model
    @Test
    public void testQuantized() {

        if(sampleData.isEmpty()){

            fail("No sample to test!");

        } else {
            model.setQuantized(true);

            for (SupportSetItem item : sampleData) {

                String labelId = item.getLabelId();
                String result = model.classifyAndReturnPredAndSimilarityMap(item.getBitmap()).first;

                Log.d("testQuantized", "Testing a sample of character: " + labelId);
                Log.d("testQuantized", "Expected: " + labelId + ", Found: " + result);

                assertEquals("Testing a sample of character \"" + labelId + "\" failed.", labelId, result);

            }
        }

    }

    @AfterClass
    public static void tearDown() {
        model.close();
    }

}
