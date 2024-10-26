package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class History {


    private static volatile History INSTANCE = null;


    private Set<HistoryItem> historyItems = new HashSet<>();
    private History() {
    }

    public static History getInstance() {
        if(INSTANCE == null) {
            synchronized (History.class) {
                if (INSTANCE == null) {
                    INSTANCE = new History();
                }
            }
        }
        return INSTANCE;
    }

    public void addItem(HistoryItem _hi){
        historyItems.add(_hi);
    }

    public List<HistoryItem> getItems(){
        return new ArrayList<>(historyItems);
    }

    /***
     *
     * @param historyItem
     * @param context
     *
     * Stores historyItem in internal storage as a lossless png under the name
     * "prediction"+random integer for uniqueness
     */
    public void saveItem(HistoryItem historyItem, Context context){
        File root = new File(context.getFilesDir(), "saved_bitmaps");
        if(!root.exists()){
            root.mkdir();
        }

        String imgFileName = historyItem.pred + "" + (int) (Math.random() * 10000) + historyItem.getModel() + ".png";
        File img_file = new File(root, imgFileName);
        try(FileOutputStream out = new FileOutputStream(img_file)){
            historyItem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("Bitmap Saved!", "Bitmap save in " + img_file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String jsonFileName = "model_outputs.json";
        File json_file = new File(root, jsonFileName);
        if (!json_file.exists()){
            try(FileOutputStream out = new FileOutputStream(json_file)){
                Log.i("model_outputs.json", "Json file created");
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        JSONObject mainJsonObject = getJsonObject(json_file);

        JSONObject similarityMap = new JSONObject();
        JSONArray cnnTensor = new JSONArray();
        String jsonString;

        try {
            if(historyItem instanceof SMSHistoryItem) {

                for (String key : (((SMSHistoryItem) historyItem).getOutputCollection()).keySet()) {
                    similarityMap.put(key, ((SMSHistoryItem) historyItem).getOutputCollection().get(key));
                }

                mainJsonObject.put(imgFileName, similarityMap);
            } else if (historyItem instanceof CNNHistoryItem) {
                for(float[] row: ((CNNHistoryItem) historyItem).getOutputCollection()) {
                    JSONArray rowJson = new JSONArray();
                    for (float value : row){
                        rowJson.put(value+"");
                    }
                    cnnTensor.put(rowJson);
                }
                mainJsonObject.put(imgFileName, cnnTensor);
            }
            jsonString = mainJsonObject.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try(FileOutputStream out = new FileOutputStream(json_file)){
            out.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static JSONObject getJsonObject(File json_file) {
        JSONObject jsonObject;

        try (FileInputStream in = new FileInputStream(json_file)){
            byte[] data = new byte[in.available()];
            in.read(data);
            String existingContent = new String(data, StandardCharsets.UTF_8);

            if(existingContent.isEmpty()){
                jsonObject = new JSONObject();
            }else{
                jsonObject = new JSONObject(existingContent);
            }
        }catch(IOException | JSONException e){
            throw new RuntimeException(e);
        }
        return jsonObject;
    }


    // fill the history set with the saved imgs
    public void updateHistory(Context context){
        File bitmapDir = new File(context.getFilesDir(), "saved_bitmaps");
        File jsonFile = new File(bitmapDir, "model_outputs.json");
        if(!bitmapDir.exists()){
            return;
        }
        historyItems.clear();
        for (File file: Objects.requireNonNull(bitmapDir.listFiles())) {
            if(file.getName().equals("model_outputs.json")) continue;
            try(FileInputStream in = new FileInputStream(file)){
                Bitmap bmp = BitmapFactory.decodeStream(in);
                String pred = file.getName().charAt(0)+"";
                if(file.getName().contains("SMS")){
                    Map<String, Float> similarityMap = getSimilarityMapFromJSON(file, jsonFile);
                    SMSHistoryItem _hi = new SMSHistoryItem(bmp, pred, similarityMap);
                    historyItems.add(_hi);
                } else if(file.getName().contains("CNN")){
                    float[][] outputTensor = getTensorFromJSON(file,jsonFile);
                    CNNHistoryItem _hi = new CNNHistoryItem(bmp, pred, outputTensor);
                    historyItems.add(_hi);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private float[][] getTensorFromJSON(File file, File source) {
        float[][] result;
        String jsonContent;
        try(FileInputStream in = new FileInputStream(source)){
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            jsonContent = new String(buffer, StandardCharsets.UTF_8);
        }catch (IOException e){
            throw new RuntimeException(e);
        }


        try {
            JSONObject jsonFile = new JSONObject(jsonContent);
            JSONArray tensor = jsonFile.getJSONArray(file.getName());
            int rows = tensor.length();
            int cols = tensor.getJSONArray(0).length();

            result = new float[rows][cols];

            for (int i = 0; i < rows; i++) {
                JSONArray row = tensor.getJSONArray(i);
                for (int j = 0; j < cols; j++) {
                    result[i][j] = (float)(row.getDouble(j));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    Map<String, Float> getSimilarityMapFromJSON(File file, File source){
        Map<String, Float> resultMap = new HashMap<>();

        String jsonContent;
        try(FileInputStream in = new FileInputStream(source)){
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            jsonContent = new String(buffer, StandardCharsets.UTF_8);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        try{
            JSONObject jsonFile = new JSONObject(jsonContent);
            JSONObject similarityMap = jsonFile.getJSONObject(file.getName());
            for (Iterator<String> it = similarityMap.keys(); it.hasNext(); ) {
                String key = it.next();
                resultMap.put(key, Float.parseFloat(similarityMap.getString(key)));
            }
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return resultMap;
    }

    public void clearHistory(Context context) {
        File bitmapDir = new File(context.getFilesDir(), "saved_bitmaps");
        if (bitmapDir.exists()) {
            for (File file : Objects.requireNonNull(bitmapDir.listFiles())) {
                if (file.delete()) {
                    Log.i("File Deleted", "Deleted file: " + file.getName());
                } else {
                    Log.e("File Deletion Failed", "Failed to delete file: " + file.getName());
                }
            }
            historyItems.clear();
            Log.i("History Cleared", "All history items have been deleted and memory cleared.");
        } else {
            Log.i("No History Found", "No history to delete.");
        }
    }
}
