package com.example.hcc_elektrobit;

import android.os.Handler;
import android.util.Log;

public class CanvasTimer implements Runnable {
    private static final String TAG = "CanvasTimer";

    private final TimeoutActivity activity;
    private final long timeoutDuration;
    private final Handler handler;

    // For the Runnable implementation
    private volatile boolean running = true;

    // For the new timer control methods
    private Runnable timerRunnable;
    private boolean timerRunning = false;

    // Constructors
    public CanvasTimer(TimeoutActivity activity) {
        this.activity = activity;
        this.timeoutDuration = 1000; // Default timeout duration
        this.handler = new Handler();
    }

    public CanvasTimer(TimeoutActivity activity, long timeoutDuration) {
        this.activity = activity;
        this.timeoutDuration = timeoutDuration;
        this.handler = new Handler();
    }

    // Runnable implementation
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

    // Timer control methods
    public void startTimer() {
        stopTimer(); // Ensure any existing timer is stopped
        timerRunnable = () -> {
            Log.d(TAG, "Timer expired, calling onTimeout()");
            activity.onTimeout();
            timerRunning = false; // Reset the flag
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