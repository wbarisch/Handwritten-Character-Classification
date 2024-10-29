package com.example.hcc_elektrobit;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Map;

public class MainViewModel extends AndroidViewModel implements TimeoutActivity {

    Timer canvasTimer;
    private MutableLiveData<Boolean> _clearCanvasEvent = new MutableLiveData<Boolean>(false);
    public LiveData<Boolean> clearCanvasEvent = _clearCanvasEvent;
    private String classifiedCharacter;
    private MutableLiveData<String> _textMessageWithResult = new MutableLiveData<>("_");
    public LiveData<String> textMessageWithResult = _textMessageWithResult;
    private MutableLiveData<Bitmap> _drawingBitmap = new MutableLiveData<Bitmap>();
    public LiveData<Bitmap> drawingBitmap = _drawingBitmap;
    private MutableLiveData<String> _executionTime = new MutableLiveData<>("0.000");
    public LiveData<String> executionTime = _executionTime;
    private final AudioPlayerManager audioPlayerManager;
    private final CNNonnxModel cnn_model;
    private final Context mainContext;
    private String modelName = "SMS";
    private boolean isQuantizedModel = false;

    public MainViewModel(Application application){

        super(application);
        mainContext = application.getApplicationContext();
        audioPlayerManager = new AudioPlayerManager(mainContext);
        cnn_model = CNNonnxModel.getInstance(mainContext);
        canvasTimer = new Timer(this, 1000);
    }

    /**
     * Classifies the character, sets and plays the audio feedback accordingly and updates the LiveData bitmap.
     * @param firstBitmap
     */
    public void mainAppFunction(Bitmap firstBitmap){

        classifyCharacterDispatcher(firstBitmap);
        String resultString = "Classified character: " + classifiedCharacter;
        _textMessageWithResult.setValue(resultString);

        String fileName = String.valueOf(classifiedCharacter);

        try{
            audioPlayerManager.setDataSource(fileName);
            audioPlayerManager.play();
        }catch(Exception e){
            Log.e("MainViewModel", "Error starting audio player manager");
        }

        saveResultBitmap();
    }

    /**
     * Dispatches to the correct classify character function according to the model needed
     * @param canvasBitmap
     */
    private void classifyCharacterDispatcher(Bitmap canvasBitmap){

        if (canvasBitmap == null) {
            Log.e("JMainActivity", "Bitmap is null in classifyCharacter");
            return;
        }

        long startTime = System.nanoTime();
        switch(modelName){
            case "SMS":
                classifyCharacterSMS(canvasBitmap);
                break;
            case "CNN":
                classifyCharacterCNN(canvasBitmap);
                break;
            default:
                Log.e("MainViewModel", "Error in classifyCharacterDispatcher: No model");
                classifiedCharacter = "";
        }
        long endTime = System.nanoTime();

        Log.i(modelName, classifiedCharacter);
        Double time = Math.round((endTime - startTime) / 1_000_000.0) / 1_000.0;
        _executionTime.setValue(String.valueOf(time));

        runOnUiThread(() -> {
            bitmapDisplay.setImageBitmap(bitmap);
        });
    }

    /**
     * Calls the necessary model methods to classify a character from the provided bitmap.
     *
     */
    private void classifyCharacterSMS(Bitmap canvasBitmap){

        History history = History.getInstance();
        Pair<String, Map<String, Float>> result_pair;

        if(isQuantizedModel){
            result_pair = SMSonnxQuantisedModel.getInstance(mainContext).classifyAndReturnPredAndSimilarityMap(canvasBitmap);
        }
        else {
            result_pair = SMSonnxModel.getInstance(mainContext).classifyAndReturnPredAndSimilarityMap(canvasBitmap);
        }
        SMSHistoryItem historyItem = new SMSHistoryItem(canvasBitmap, result_pair.first.toString(), result_pair.second);
        history.saveItem(historyItem, mainContext);
        classifiedCharacter = result_pair.first.toString();
        Log.i(modelName, result_pair.second.toString());
    }

    private void classifyCharacterCNN(Bitmap canvasBitmap){

        History history = History.getInstance();
        Pair<Integer, float[][]> result_pair = cnn_model.classifyAndReturnIntAndTensor(canvasBitmap);

        CNNHistoryItem historyItemCNN = new CNNHistoryItem(canvasBitmap, result_pair.first.toString(), result_pair.second);
        history.saveItem(historyItemCNN, mainContext);
        classifiedCharacter = result_pair.first.toString();
        Log.i(modelName, Arrays.deepToString(result_pair.second));
    }

    /**
     * Stores the current bitmap in the history.
     */
    private void saveResultBitmap(){

        History history = History.getInstance();
        String classifiedDigit = _textMessageWithResult.getValue();
        int helperLength = classifiedDigit.length();
        classifiedDigit = classifiedDigit.substring(helperLength - 1);
        HistoryItem historyItem = new HistoryItem(drawingBitmap.getValue(), classifiedDigit);
        history.saveItem(historyItem, mainContext);
    }

    private Bitmap createBitmapFromFloatArray(float[] floatArray, int width, int height){

        if(floatArray.length != width * height){
            throw new IllegalArgumentException("Float array length must match width * height");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];

        for (int i = 0; i < floatArray.length; i++) {

            float value = floatArray[i];
            value = Math.max(0, Math.min(1, value));
            int grayscale = (int) (value * 255);
            int color = Color.argb(255, grayscale, grayscale, grayscale);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    // Method to start the custom Timer
    public void startTimer(int durationMillis) {
        if (canvasTimer != null) {
            canvasTimer.cancel(); // Cancel any existing timer
        }

        // Create a new Timer instance and start it in a separate thread
        canvasTimer = new Timer(this, durationMillis);
        new Thread(canvasTimer).start();
    }

    // Method to cancel the Timer
    public void cancelTimer() {
        if (canvasTimer != null) {
            canvasTimer.cancel();
        }
    }
    public void onTimeout() {
        _clearCanvasEvent.postValue(true); // Notify that the canvas should be cleared
    }

    public void clearCanvasHandled() {
        _clearCanvasEvent.postValue(false);
    }
}
