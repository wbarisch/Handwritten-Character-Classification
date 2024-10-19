package com.example.hcc_elektrobit;

import android.util.Log;

public class CanvasTimer implements Runnable {

    private static final String TAG = "CanvasTimer";
    private final TimeoutActivity activity;
    private volatile boolean running = true;
    public CanvasTimer(TimeoutActivity activity){

        this.activity = activity;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interrupted while sleeping", e);
        }

        if(running) {
            activity.onTimeout();
        }

    }

    public void cancel() {

        running = false;

    }

}
