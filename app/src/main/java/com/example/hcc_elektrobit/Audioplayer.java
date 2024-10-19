package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;

public class AudioPlayer {
    private final MediaPlayer mp;
    private final Context context;
    private static final String TAG = "AudioPlayer";
    public  AudioPlayer(Context context){
        this.mp = new MediaPlayer();
        this.context = context;
    }

    public void PlayAudio(String recognisedChar){

        try {

            AssetManager assetManager = context.getAssets();

            AssetFileDescriptor afd = assetManager.openFd("digits_audio"+ File.separator + recognisedChar + ".aac");
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();

            afd.close();
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio for character: " + recognisedChar, e);
        }
    }
}
