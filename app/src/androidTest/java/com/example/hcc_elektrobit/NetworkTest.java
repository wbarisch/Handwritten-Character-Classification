package com.example.hcc_elektrobit;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.*;

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

    // Use the app's SupportSet as a sample data
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

    @AfterClass
    public static void tearDown() {
        model.close();
    }

}
