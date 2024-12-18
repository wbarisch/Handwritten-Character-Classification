package com.example.hcc_elektrobit.shared;

import android.app.Application;
import android.content.Context;

import com.example.hcc_elektrobit.support_set.SupportSet;


// Application class to provide to context to the file provider
public class HCC_Application extends Application {

    private static volatile HCC_Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static Context getAppContext(){
        return INSTANCE.getApplicationContext();
    }

    protected Application getApplication(){
        return this;
    }

    // Expose internal variable through getter for testing.
    public static SupportSet getSupportSet(){

        SupportSet supportSet = SupportSet.getInstance();

        supportSet.updateSet();

        return supportSet;

    }

}
