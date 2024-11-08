package com.example.hcc_elektrobit;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NetworkTest {

    // Context for the target application being instrumented
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    // The Siamese model
    private SMSComaparisonOnnxModel model = SMSComaparisonOnnxModel.getInstance();

    //
    private SupportSet supportSet = HCC_Application.getSupportSet();

}
