package com.example.hcc_elektrobit;

import android.util.Log;

public class Timer implements Runnable {
    private final String TAG;
    private final TimeoutActivity activity;
    private volatile boolean running = true;
    private int waitTime;
    public Timer(TimeoutActivity activity, int millisTime){
        waitTime = millisTime;
        TAG = activity.toString() + " TIMER";
        this.activity = activity;
    }

    public void changeWaitTime(int waitTimeMillis){
        waitTime = waitTimeMillis;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(waitTime);
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
