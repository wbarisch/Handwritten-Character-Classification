package com.example.hcc_elektrobit;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EvaluationViewModel extends AndroidViewModel {

    private final MutableLiveData<String> evaluationResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, List<String>>> mispredictions = new MutableLiveData<>();


    private SMSonnxModel singleStepModel;
    private SMSonnxQuantisedModel singleStepQuanModel;
    private SMSComaparisonOnnxModel twoStepModel;
    private SMSComaparisonOnnxModel twoStepQuanModel;

    private int TEST_SIZE = 0;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> evaluationFuture;
    private boolean testRunning = false;

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
        List<String> modelList = Arrays.asList("normal", "quantized", "2-step", "2-step quantized");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                HCC_Application.getAppContext(),
                android.R.layout.simple_spinner_item,
                modelList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    protected void reinistializeTestData(){
        deletedFolder("test_data");
        loadTestData();

    }



    protected void start_test(String model) {
        if(!testRunning) {
            testRunning = true;
            Toast.makeText(HCC_Application.getAppContext(), "Started evaluation using " + model + " model...", Toast.LENGTH_SHORT).show();
            evaluationFuture = executorService.submit(() -> {
                Pair<Float, Map<String, List<String>>> returned_pair = null;
                float accuracy = 0f;
                float elapsed = 0f;

                switch (model) {
                    case "normal": {
                        singleStepModel = SMSonnxModel.getInstance(HCC_Application.getAppContext());
                        long startTime = System.currentTimeMillis();

                        returned_pair = test(singleStepModel);
                        if (returned_pair != null)
                            accuracy = returned_pair.first;

                        long endTime = System.currentTimeMillis();
                        elapsed = endTime - startTime;

                        Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                        Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");

                        break;
                    }
                    case "quantized": {
                        singleStepQuanModel = SMSonnxQuantisedModel.getInstance(HCC_Application.getAppContext());
                        long startTime = System.currentTimeMillis();

                        returned_pair = test(singleStepQuanModel);
                        if (returned_pair != null)
                            accuracy = returned_pair.first;


                        long endTime = System.currentTimeMillis();
                        elapsed = endTime - startTime;

                        Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                        Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");
                        break;
                    }
                    case "2-step": {
                        SMSComaparisonOnnxModel.getInstance().setQuantized(false);
                        twoStepModel = SMSComaparisonOnnxModel.getInstance();
                        long startTime = System.currentTimeMillis();

                        returned_pair = test(twoStepModel);
                        if (returned_pair != null)
                            accuracy = returned_pair.first;


                        long endTime = System.currentTimeMillis();
                        elapsed = endTime - startTime;

                        Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                        Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");
                        break;
                    }
                    case "2-step quantized": {
                        SMSComaparisonOnnxModel.getInstance().setQuantized(true);
                        twoStepQuanModel = SMSComaparisonOnnxModel.getInstance();
                        long startTime = System.currentTimeMillis();

                        returned_pair = test(twoStepQuanModel);
                        if (returned_pair != null)
                            accuracy = returned_pair.first;


                        long endTime = System.currentTimeMillis();
                        elapsed = endTime - startTime;

                        Log.e("Test", "Time it took was " + elapsed / 1000 + " s");
                        Log.e("Test", "Time per classification was " + (elapsed / 1000) / TEST_SIZE + " s");
                        break;
                    }
                    default:
                        break;
                }

                if (returned_pair != null) {
                    evaluationResult.postValue("Using the " + model + " model, the accuracy for " + TEST_SIZE +
                            " images was " + accuracy + " and took " + elapsed / 1000 + " s"
                            + " (" + (elapsed / 1000) / TEST_SIZE + " s per classification)");

                    mispredictions.postValue(returned_pair.second);
                }
                testRunning = false;
            });
        }
    }

    private Pair<Float, Map<String, List<String>>> test(SMSonnxModel singleStep) {
        int correctPrediction = 0;
        Map<String, List<String>> misPredictions_value = new HashMap<>();
        File testDataFolder = new File(JFileProvider.getInternalDir(), "test_data");
        if (testDataFolder.exists() && testDataFolder.isDirectory()) {
            File[] pngs = testDataFolder.listFiles();

            if (pngs != null) {
                for (File png : pngs) {

                    if (Thread.currentThread().isInterrupted()) {
                        Log.d("Test", "Evaluation canceled");
                        return null;
                    }

                    try (InputStream is = new FileInputStream(png)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

                        if (bitmap != null) {
                            String result = singleStep.classify_id(bitmap);
                            String expected = png.getName().charAt(0) + "";
                            if (result.equals(expected)) {
                                correctPrediction++;
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
        }else{
            Log.e("Model Evaluation", "No test folder found!");
        }
        return new Pair<>((float)correctPrediction / TEST_SIZE, misPredictions_value);
    }

    private Pair<Float, Map<String, List<String>>> test(SMSonnxQuantisedModel singleStep_quan) {
        int correctPrediction = 0;
        Map<String, List<String>> misPredictions_value = new HashMap<>();
        File testDataFolder = new File(JFileProvider.getInternalDir(), "test_data");
        if (testDataFolder.exists() && testDataFolder.isDirectory()) {
            File[] pngs = testDataFolder.listFiles();

            if (pngs != null) {
                for (File png : pngs) {

                    if (Thread.currentThread().isInterrupted()) {
                        Log.d("Test", "Evaluation canceled");
                        return null;
                    }

                    try (InputStream is = new FileInputStream(png)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

                        if (bitmap != null) {
                            String result = singleStep_quan.classify_id(bitmap);
                            String expected = png.getName().charAt(0) + "";
                            if (result.equals(expected)) {
                                correctPrediction++;
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
        }else{
            Log.e("Model Evaluation", "No test folder found!");
        }
        return new Pair<>((float)correctPrediction / TEST_SIZE, misPredictions_value);
    }

    private Pair<Float, Map<String, List<String>>> test(SMSComaparisonOnnxModel twoStep) {
        int correctPrediction = 0;
        TEST_SIZE = 0;
        Map<String, List<String>> misPredictions_value = new HashMap<>();
        File testDataFolder = new File(JFileProvider.getInternalDir(), "test_data");
        if (testDataFolder.exists() && testDataFolder.isDirectory()) {
            File[] pngs = testDataFolder.listFiles();

            if (pngs != null) {
                for (File png : pngs) {

                    if (Thread.currentThread().isInterrupted()) {
                        Log.d("Test", "Evaluation canceled");
                        return null;
                    }

                    try (InputStream is = new FileInputStream(png)) {
                        TEST_SIZE++;
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

                        if (bitmap != null) {
                            String result = String.valueOf(twoStep.classifyAndReturnPredAndSimilarityMap(bitmap).first.charAt(0));
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
        }else{
            Log.e("Model Evaluation", "No test folder found!");
        }
        return new Pair<>((float)correctPrediction / TEST_SIZE, misPredictions_value);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelEval();
    }

    protected void cancelEval(){
        Toast.makeText(HCC_Application.getAppContext(), "Cancelling evaluation... (can take up to 3 seconds)", Toast.LENGTH_LONG).show();
        if (evaluationFuture != null && !evaluationFuture.isDone()) {
            evaluationFuture.cancel(true); // Cancel the task if it’s running
        }
    }

    public void loadTestData() {
        File supportSetDir = new File(JFileProvider.getInternalDir(), "test_data");

        if (!supportSetDir.exists()) {
            supportSetDir.mkdir();
            copyAssetsToInternal("test_data");
            Log.i("Initialization", "Copied test_data folder from assets to internal storage.");
        } else {
            Log.i("Initialization", "test_data folder already exists in internal storage.");
        }
    }

    private void deletedFolder(String folderName) {
        File dir = new File(JFileProvider.getInternalDir(), folderName);

        if (dir.exists() && dir.isDirectory()) {
            deleteRecursive(dir);
            Log.i("Folder Deleted", "Folder " + folderName + " and all its contents have been deleted.");
        } else {
            Log.e("Folder Not Found", "Folder " + folderName + " does not exist or is not a directory.");
        }

    }
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (fileOrDirectory.delete()) {
            Log.i("File Deleted", "Deleted: " + fileOrDirectory.getName());
        } else {
            Log.e("File Deletion Failed", "Failed to delete: " + fileOrDirectory.getName());
        }
    }

    private void copyAssetsToInternal(String folderName) {
        AssetManager assetManager = JFileProvider.getAssets();
        try {
            String[] files = assetManager.list(folderName);
            if (files != null) {
                for (String fileName : files) {
                    try (InputStream in = assetManager.open(folderName + "/" + fileName);
                         FileOutputStream out = new FileOutputStream(new File(JFileProvider.getInternalDir(), folderName + "/" + fileName))) {

                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    Log.i("File Copied", "Copied " + fileName + " to internal storage.");
                }
            }
        } catch (IOException e) {
            Log.e("Asset Copy Error", "Error copying assets to internal storage", e);
        }
    }
}
