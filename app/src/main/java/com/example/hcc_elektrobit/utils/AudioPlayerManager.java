package com.example.hcc_elektrobit.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;

public class AudioPlayerManager {
    private MediaPlayer mediaPlayer;
    private final Context context;
    private static final String TAG = "AudioPlayerManager";
    private static final String audioFilesPath = "digits_audio"+ File.separator;
    private String currentFileName;
    private AssetManager assetManager;

    /**
     * The class is responsible for Audio playing functions and managing which of the audio files to use.
     * @param appContext Needed to access the resources of the project
     */
    public AudioPlayerManager(Context appContext){
        this.mediaPlayer = new MediaPlayer();
        this.context = appContext;
        assetManager = context.getAssets();
    }

    // Must go
//    public void initializePlayer(int resourceId){
//        mediaPlayer = MediaPlayer.create(context, resourceId);
//    }

    /**
     * This method changes the source audio file to a new one and prepares for the audio play.
     *
     */
    public void setDataSource(String fileName){

        fileName = String.valueOf(fileName.charAt(0)).toUpperCase();

        if(fileName.equals(currentFileName))
            return;

        currentFileName = fileName;
        try{
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(audioFilesPath + fileName + ".aac");
            mediaPlayer.reset();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mediaPlayer.prepare();
            assetFileDescriptor.close();
        }catch (Exception e){
            Log.e(TAG, "Error playing audio file: " + currentFileName , e);
        }
    }

    public void play(){
        if(mediaPlayer != null && !mediaPlayer.isPlaying()){
            //mediaPlayer.start();
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
}
