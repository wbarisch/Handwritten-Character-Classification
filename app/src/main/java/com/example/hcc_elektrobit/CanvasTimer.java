package com.example.hcc_elektrobit;

import android.os.Handler;
import android.util.Log;

public class CanvasTimer implements Runnable {
    private static final String TAG = "CanvasTimer";

    private final TimeoutActivity activity;
    private final long timeoutDuration;
    private final Handler handler;

    private volatile boolean running = true;

    private Runnable timerRunnable;
    private boolean timerRunning = false;

    // Constructors
    public CanvasTimer(TimeoutActivity activity) {
        this.activity = activity;
        this.timeoutDuration = 1000;
        this.handler = new Handler();
    }

    public CanvasTimer(TimeoutActivity activity, long timeoutDuration) {
        this.activity = activity;
        this.timeoutDuration = timeoutDuration;
        this.handler = new Handler();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeoutDuration);
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interrupted while sleeping", e);
        }
        if (running) {
            activity.onTimeout();
        }
    }

    public void cancel() {
        running = false;
    }
    public void startTimer() {
        stopTimer();
        timerRunnable = () -> {
            Log.d(TAG, "Timer expired, calling onTimeout()");
            activity.onTimeout();
            timerRunning = false;
        };
        handler.postDelayed(timerRunnable, timeoutDuration);
        timerRunning = true;
        Log.d(TAG, "Timer started for " + timeoutDuration + " milliseconds");
    }

    public void stopTimer() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
            timerRunnable = null;
            timerRunning = false;
            Log.d(TAG, "Timer stopped");
        }
    }

    public void resetTimer() {
        Log.d(TAG, "Timer reset");
        startTimer();
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }
}