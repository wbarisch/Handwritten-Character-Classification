package com.example.hcc_elektrobit;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationViewModel extends AndroidViewModel {

    private final MutableLiveData<String> evaluationResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, List<String>>> mispredictions = new MutableLiveData<>();


    private SMSonnxModel singleStepModel;
    private SMSComaparisonOnnxModel twoStepModel;

    private final int TEST_SIZE = 111;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public EvaluationViewModel(Application application) {
        super(application);
    }

    protected LiveData<String> getEvaluationResult() {
        return evaluationResult;
    }

    protected LiveData<Map<String, List<String>>> getMispredictions(){
        return mispredictions;
    }

    protected void setupSpinner(Spinner modelSpinner) {
        List<String> modelList = Arrays.asList("normal", "2-step", "2-step quantized");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                HCC_Application.getAppContext(),
                android.R.layout.simple_spinner_item,
                modelList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    protected void start_test(String model) {
        executorService.execute(() -> {
            Pair<Float, Map<String, List<String>>> returned_pair = null;
            float accuracy = 0f;
            float elapsed = 0f;

            if (model.equals("normal")) {
                singleStepModel = SMSonnxModel.getInstance(HCC_Application.getAppContext());
                long startTime = System.currentTimeMillis();

                accuracy = test(singleStepModel);

                long endTime = System.currentTimeMillis();
                elapsed = endTime - startTime;

                Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");
            } else if (model.equals("2-step")) {
                twoStepModel = SMSComaparisonOnnxModel.getInstance();
                long startTime = System.currentTimeMillis();

                returned_pair = test(twoStepModel);
                accuracy = returned_pair.first;


                long endTime = System.currentTimeMillis();
                elapsed = endTime - startTime;

                Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");
            }

            // Update the evaluation result on the main thread
            evaluationResult.postValue(accuracy  + " and took " + elapsed/1000 + " s"
                    +" ("+(elapsed / 1000) / TEST_SIZE + " s per classification)" );

            mispredictions.postValue(returned_pair.second);
        });
    }

    private float test(SMSonnxModel singleStep) {
        int correctPrediction = 0;
        File testDataFolder = new File(JFileProvider.getInternalDir(), "test_data");
        if (testDataFolder.exists() && testDataFolder.isDirectory()) {
            File[] pngs = testDataFolder.listFiles();

            if (pngs != null) {
                for (File png : pngs) {

                    if (Thread.currentThread().isInterrupted()) {
                        Log.d("Test", "Evaluation canceled");
                        return (float) correctPrediction / TEST_SIZE;
                    }

                    try (InputStream is = new FileInputStream(png)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

                        if (bitmap != null) {
                            String result = singleStep.classify_id(bitmap);
                            if (result.equals(png.getName().charAt(0) + "")) {
                                correctPrediction++;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return (float)correctPrediction / TEST_SIZE;
    }

    private Pair<Float, Map<String, List<String>>> test(SMSComaparisonOnnxModel twoStep) {
        int correctPrediction = 0;
        Map<String, List<String>> misPredictions_value = new HashMap<>();
        File testDataFolder = new File(JFileProvider.getInternalDir(), "test_data");
        if (testDataFolder.exists() && testDataFolder.isDirectory()) {
            File[] pngs = testDataFolder.listFiles();

            if (pngs != null) {
                for (File png : pngs) {

                    if (Thread.currentThread().isInterrupted()) {
                        Log.d("Test", "Evaluation canceled");
                        return new Pair<>((float) correctPrediction / TEST_SIZE, misPredictions_value);
                    }

                    try (InputStream is = new FileInputStream(png)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

                        if (bitmap != null) {
                            String result = twoStep.classifyAndReturnPredAndSimilarityMap(bitmap).first;
                            String expected = png.getName().charAt(0) + "";
                            Log.d("testAct", "tested " + png.getName());
                            Log.d("predicted", result);
                            if (result.equals(expected)) {
                                correctPrediction++;
                                Log.e("Test", "correct!");
                            }else{
                                if(!misPredictions_value.containsKey(expected)){
                                    List<String> tempArray = new ArrayList<>();
                                    tempArray.add(result);
                                    misPredictions_value.put(expected, tempArray);
                                }else{
                                    misPredictions_value.get(expected).add(result);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        Log.e("acc", (float) correctPrediction / TEST_SIZE + "");
        return new Pair<>((float)correctPrediction / TEST_SIZE, misPredictions_value);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown the executor service when the ViewModel is cleared to avoid memory leaks
        cancelEval();
    }

    protected void cancelEval(){
        executorService.shutdown();
    }
}
