package com.example.hcc_elektrobit;

import android.app.Application;
import android.content.Context;


// Application class to provide to context to the file provider
public class HCC_Application extends Application {

    private static volatile HCC_Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    protected static Context getAppContext(){
        return INSTANCE.getApplicationContext();
    }

    protected Application getApplication(){
        return this;
    }
}
