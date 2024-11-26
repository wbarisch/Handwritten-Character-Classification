package com.example.hcc_elektrobit;

import android.app.Activity;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DialogManagerTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private DialogManager dialogManager;

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager = new DialogManager(activity);
        });
    }

    @Test
    public void testShowShareOrSaveDialog() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager.showShareOrSaveDialog();
            assertNotNull(dialogManager);
        });
    }

    @Test
    public void testShowTrainingModeDialog() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager.showTrainingModeDialog();
            assertNotNull(dialogManager);
        });
    }

    @Test
    public void testShowExitTrainingModeDialog() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager.showExitTrainingModeDialog(() -> {
                // Action to take on confirm exit; testing no exceptions thrown
            });
            assertNotNull(dialogManager);
        });
    }

    @Test
    public void testShowNoImagesDialog() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager.showNoImagesDialog();
            assertNotNull(dialogManager);
        });
    }

    @Test
    public void testShowToggleBitmapModeDialog() {
        activityRule.getScenario().onActivity(activity -> {
            dialogManager.showToggleBitmapModeDialog(true, mode -> {
                // Test callback for mode change
            });
            assertNotNull(dialogManager);
        });
    }
}
