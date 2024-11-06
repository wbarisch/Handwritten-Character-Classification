package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AudioPlayerManagerTest {

    private AudioPlayerManager audioPlayerManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        audioPlayerManager = new AudioPlayerManager(context);
        AssetManager assetManager = context.getAssets();
    }

    @After
    public void tearDown() {
        audioPlayerManager.stop();
    }

    @Test
    public void testSetDataSource_validFile() {
        String validFileName = "1";

        try {
            audioPlayerManager.setDataSource(validFileName);
            // The MediaPlayer should be prepared without exceptions
            assertNotNull(audioPlayerManager);
        } catch (Exception e) {
            fail("Exception occurred while setting data source for valid file: " + e.getMessage());
        }
    }

    @Test
    public void testSetDataSource_invalidFile() {
        // This file does not exist
        String invalidFileName = "nonexistent";

        try {
            audioPlayerManager.setDataSource(invalidFileName);
            fail("Expected exception was not thrown for invalid file name");
        } catch (Exception e) {
            // Expected an exception, so the test passes if we catch one
            assertNotNull(e);
        }
    }

    @Test
    public void testPlay() {
        String validFileName = "1";
        audioPlayerManager.setDataSource(validFileName);
        audioPlayerManager.play();

        assertTrue("MediaPlayer should be playing", audioPlayerManager.isPlaying());
    }

    @Test
    public void testPause() {
        String validFileName = "1";
        audioPlayerManager.setDataSource(validFileName);
        audioPlayerManager.play();
        audioPlayerManager.pause();

        assertFalse("MediaPlayer should be paused", audioPlayerManager.isPlaying());
    }

    @Test
    public void testStop() {
        String validFileName = "1";
        audioPlayerManager.setDataSource(validFileName);
        audioPlayerManager.play();
        audioPlayerManager.stop();

        assertFalse("MediaPlayer should be stopped", audioPlayerManager.isPlaying());
    }
}
