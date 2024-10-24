package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;

public class AudioPlayerManager {
    private final MediaPlayer mediaPlayer;
    private final Context context;
    private static final String TAG = "AudioPlayerManager";
    public AudioPlayerManager(Context appContext){
        this.mediaPlayer = new MediaPlayer();
        this.context = appContext;
    }

    public void setDataSource(String filePath){
        AssetFileDescriptor assetFileDescriptor = assetManager.openFd
    }
    public void play(){
        if(mediaPlayer != null && !mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    public void pause(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    public void stop(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
    }

    public void PlayAudio(String recognisedChar){

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd("digits_audio"+ File.separator + recognisedChar + ".aac");

            mediaPlayer.reset();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();

            assetFileDescriptor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio for character: " + recognisedChar, e);
        }
    }
}
