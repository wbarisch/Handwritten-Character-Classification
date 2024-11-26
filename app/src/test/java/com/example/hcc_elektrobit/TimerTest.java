package com.example.hcc_elektrobit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;

public class TimerTest {

    private Timer timer;
    private TestTimeoutActivity testActivity;
    private int waitTimeMillis = 100;

    @Before
    public void setUp() {
        // Instantiate the custom TimeoutActivity
        testActivity = new TestTimeoutActivity();
        timer = new Timer(testActivity, waitTimeMillis);
    }

    @Test
    public void testOnTimeoutCalledAfterWaitTime() throws InterruptedException {
        Thread timerThread = new Thread(timer);
        timerThread.start();

        Thread.sleep(waitTimeMillis + 50);

        assertTrue(testActivity.isTimeoutCalled());
    }

    @Test
    public void testCancelPreventsOnTimeout() throws InterruptedException {
        Thread timerThread = new Thread(timer);
        timerThread.start();

        timer.cancel();

        Thread.sleep(waitTimeMillis + 50);

        assertTrue(!testActivity.isTimeoutCalled());
    }

    @Test
    public void testChangeWaitTime() throws InterruptedException {
        int newWaitTimeMillis = 200;
        timer.changeWaitTime(newWaitTimeMillis);

        Thread timerThread = new Thread(timer);
        timerThread.start();

        Thread.sleep(newWaitTimeMillis + 50);

        assertTrue(testActivity.isTimeoutCalled());
    }

    // just for testing
    private static class TestTimeoutActivity implements TimeoutActivity {
        private boolean timeoutCalled = false;

        @Override
        public void onTimeout() {
            timeoutCalled = true;
        }

        public boolean isTimeoutCalled() {
            return timeoutCalled;
        }
    }
}
