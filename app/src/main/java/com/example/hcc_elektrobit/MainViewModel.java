package com.example.hcc_elektrobit;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel implements TimeoutActivity {

    Timer canvasTimer;
    private MutableLiveData<Boolean> _clearCanvasEvent = new MutableLiveData<Boolean>(false);
    public LiveData<Boolean> clearCanvasEvent = _clearCanvasEvent;
    private MutableLiveData<String> _classifiedCharacter = new MutableLiveData<>("Classified character: ");
    public LiveData<String> classifiedCharacter = _classifiedCharacter;
    private MutableLiveData<Bitmap> _drawingBitmap = new MutableLiveData<Bitmap>();
    public LiveData<Bitmap> drawingBitmap = _drawingBitmap;
    private final JFileProvider fileProvider = new JFileProvider();
    private final AudioPlayerManager audioPlayerManager;
    private final CNNonnxModel model;
    private final Context mainContext;

    public MainViewModel(Application application){
        super(application);
        mainContext = application.getApplicationContext();
        audioPlayerManager = new AudioPlayerManager(mainContext);
        model = new CNNonnxModel(mainContext);
        canvasTimer = new Timer(this, 1000);
    }

    /**
     * Classifies the character, sets and plays the audio feedback accordingly and updates the LiveData bitmap.
     * @param firstBitmap
     */
    public void mainAppFunction(Bitmap firstBitmap){

        int result = classifyCharacter(firstBitmap);
        String resultString = "Classified character: " + result;
        _classifiedCharacter.setValue(resultString);

        String fileName = String.valueOf(result);

        try{
            audioPlayerManager.setDataSource(fileName);
            audioPlayerManager.play();
        }catch(Exception e){
            Log.e("MainViewModel", "Error starting audio player manager");
        }

        _drawingBitmap.setValue(createBitmapFromFloatArray(model.preprocessBitmap(firstBitmap), 28, 28));
        saveResult();
    }

    /**
     * Calls the necessary model methods to classify a character from the provided bitmap.
     * @param firstBitmap
     * @return Currently, it is a digit in the int Type
     */
    public int classifyCharacter(Bitmap firstBitmap){

        if(firstBitmap == null){
            Log.e("MainViewModel", "Bitmap is passed null to ClassifyCharacter.");
            return 0;
        }

        return model.classifyAndReturnDigit(firstBitmap);
    }

    /**
     * Stores the current bitmap in the history.
     */
    private void saveResult(){
        History history = History.getInstance();
        String classifiedDigit = _classifiedCharacter.getValue();
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
