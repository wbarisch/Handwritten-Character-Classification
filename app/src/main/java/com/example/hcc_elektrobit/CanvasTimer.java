package com.example.hcc_elektrobit;

public class CanvasTimer implements Runnable {

    private final TimeoutActivity activity;

    private volatile boolean running = true; // Volatile to ensure visibility across threads

    public CanvasTimer(TimeoutActivity activity){

        this.activity = activity;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(running) {
            activity.onTimeout();
        }

    }

    public void cancel() {

        running = false; // Signal the thread to stop

    }

}
