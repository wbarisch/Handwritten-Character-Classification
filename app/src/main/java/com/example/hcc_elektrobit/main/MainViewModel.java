package com.example.hcc_elektrobit.main;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.utils.AudioPlayerManager;
import com.example.hcc_elektrobit.model.SMSComaparison;
import com.example.hcc_elektrobit.utils.TimeoutActivity;
import com.example.hcc_elektrobit.utils.Timer;
import com.example.hcc_elektrobit.history.History;
import com.example.hcc_elektrobit.history.SMSHistoryItem;
import com.example.hcc_elektrobit.shared.HCC_Application;

import java.util.Map;

public class MainViewModel extends AndroidViewModel implements TimeoutActivity {

    Timer canvasTimer;
    private MutableLiveData<Boolean> _clearCanvasEvent = new MutableLiveData<Boolean>(false);
    public LiveData<Boolean> clearCanvasEvent = _clearCanvasEvent;
    private MutableLiveData<String> _classifiedCharacter = new MutableLiveData<>("_");
    public LiveData<String> classifiedCharacter = _classifiedCharacter;
    private MutableLiveData<Bitmap> _drawingBitmap = new MutableLiveData<Bitmap>();
    public LiveData<Bitmap> drawingBitmap = _drawingBitmap;
    private MutableLiveData<String> _executionTime = new MutableLiveData<>("0.000");
    public LiveData<String> executionTime = _executionTime;
    private final AudioPlayerManager audioPlayerManager;
    private MutableLiveData<Integer> bitmapSize = new MutableLiveData<>(105);
    private final Context mainContext;
    private String modelName = "SMS";


    public MainViewModel(Application application){

        super(application);
        mainContext = HCC_Application.getAppContext();
        audioPlayerManager = new AudioPlayerManager(mainContext);
        canvasTimer = new Timer(this, 1000);
    }

    /**
     * Classifies the character, sets and plays the audio feedback accordingly and updates the LiveData bitmap.
     * @param firstBitmap
     */
    public void mainAppFunction(Bitmap firstBitmap){
        if(!SupportSet.getInstance().imagesLoaded()){
            Toast.makeText(mainContext,"Images still loading", Toast.LENGTH_SHORT).show();
            return;
        }
        _drawingBitmap.setValue(firstBitmap);
        classifyCharacterDispatcher(firstBitmap);
        String fileName = classifiedCharacter.getValue();

        try{
            audioPlayerManager.setDataSource(fileName);
            audioPlayerManager.play();
        }catch(Exception e){
            Log.e("MainViewModel", "Error starting audio player manager");
        }
    }

    //region Classify character Methods
    /**
     * Dispatches to the correct classify character function according to the model needed
     * @param canvasBitmap Expected to be the most current state of a canvas bitmap
     */
    private void classifyCharacterDispatcher(Bitmap canvasBitmap){

        if (canvasBitmap == null) {
            Log.e("JMainActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        long startTime = System.nanoTime();

        classifyCharacterSMS(canvasBitmap);




        long endTime = System.nanoTime();

        Log.i(modelName, classifiedCharacter.getValue());
        Double time = Math.round((endTime - startTime) / 1_000_000.0) / 1_000.0;
        _executionTime.setValue(String.valueOf(time));

    }

    /**
     * Classify character method for the SMS model function calls.
     */
    private void classifyCharacterSMS(Bitmap canvasBitmap){

        History history = History.getInstance();
        Pair<String, Map<String, Float>> result_pair;


        result_pair = SMSComaparison.getInstance().classifyAndReturnPredAndSimilarityMap(canvasBitmap);

        SMSHistoryItem historyItem = new SMSHistoryItem(canvasBitmap, result_pair.first.toString(), result_pair.second);
        history.saveItem(historyItem, mainContext);
        _classifiedCharacter.setValue(result_pair.first);
        Log.i(modelName, result_pair.second.toString());
    }




    //region Support Methods

    /**
     * Starts a new timer with the provided value in milliseconds
     * @param duration Time duration in milliseconds
     */
    public void startTimer(int duration) {
        if (canvasTimer != null) {
            canvasTimer.cancel(); // Cancel any existing timer
        }

        // Create a new Timer instance and start it in a separate thread
        canvasTimer = new Timer(this, duration);
        new Thread(canvasTimer).start();
    }

    public void stopTimer() {
        if (canvasTimer != null) {
            canvasTimer.cancel(); // Cancel any existing timer
        }
    }

    public void onTimeout() {
        _clearCanvasEvent.postValue(true); // Notify that the canvas should be cleared
    }

    public void clearCanvasHandled() {
        _clearCanvasEvent.postValue(false);
    }

    public LiveData<Integer> getBitmapSize() {
        return bitmapSize;
    }

    public void setBitmapSize(int size) {
        bitmapSize.setValue(size);
    }


    //endregion
}
