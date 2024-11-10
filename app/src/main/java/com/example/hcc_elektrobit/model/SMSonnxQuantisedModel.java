package com.example.hcc_elektrobit.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Pair;

import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.support_set.SupportSetItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class SMSonnxQuantisedModel {
    private static SMSonnxQuantisedModel INSTANCE = null;

    private OrtEnvironment env;
    private OrtSession session;
    private final Context context;
    private static final String TAG = "SMSonnxModel";

    private SMSonnxQuantisedModel(Context context) {
        this.context = context;
        try {
            String modelPath = copyModelToCache();
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            Log.i(TAG, "ONNX session created successfully.");
        } catch (OrtException e) {
            Log.e(TAG, "Error creating ONNX session", e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading ONNX model from assets", e);
        }
    }

    public static SMSonnxQuantisedModel getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SMSonnxQuantisedModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SMSonnxQuantisedModel(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private String copyModelToCache() throws IOException {
        String modelFileName = "siamese_model_mine_245_prerocc_quantized.onnx";
        File cacheDir = context.getCacheDir();
        File modelFile = new File(cacheDir, modelFileName);

        if (!modelFile.exists()) {
            try (InputStream is = context.getAssets().open(modelFileName);
                 FileOutputStream fos = new FileOutputStream(modelFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
        return modelFile.getAbsolutePath();
    }
    public float[][] findSimilarity(Bitmap bitmap, Bitmap bitmap2) {
        try {

            float[] input1TensorData = preprocessBitmap(bitmap);
            float[] input2TensorData = preprocessBitmap(bitmap2);
            OnnxTensor inputTensor1 = OnnxTensor.createTensor(env, FloatBuffer.wrap(input1TensorData), new long[]{1, 1, 105, 105});
            OnnxTensor inputTensor2 = OnnxTensor.createTensor(env, FloatBuffer.wrap(input2TensorData), new long[]{1, 1, 105, 105});

            Map<String, OnnxTensor> inputMap = new HashMap<>();
            inputMap.put("input1", inputTensor1);  // Use the input name "input1" as defined in the model export
            inputMap.put("input2", inputTensor2);  // Use the input name "input2" as defined in the model export

            // Run the ONNX session with the inputs
            OrtSession.Result result = session.run(inputMap);

            // Get the output from the result
            float[][] output = (float[][]) result.get(0).getValue();

            // Log output information for debugging
            Log.i(TAG, "Output Tensor Shape: [" + output.length + ", " + output[0].length + "]");
            Log.i(TAG, "Output Tensor Values: " + java.util.Arrays.toString(output[0]));


            //float [] output = {1.0f,2.9f,3.0f};
            return output;

        } catch (OrtException e) {
            Log.e(TAG, "Error during classification", e);
        }
        return null;
    }

    public float classify_similarity(Bitmap bitmap1,Bitmap bitmap2){
        float[][] result = this.findSimilarity(bitmap1,bitmap2);
        return result[0][0];
    }

    public String classify_id(Bitmap bitmap1){

        SupportSet.getInstance().updateSet();

        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems();

        Map<String, List<Float>> similarityMap = new HashMap<>();

        for (SupportSetItem item : supportSet) {
            String labelId = item.getLabelId();
            Bitmap bitmap2 = item.getBitmap();

            float[][] result = findSimilarity(bitmap1, bitmap2);
            float similarity = result[0][0] + 100f;

            similarityMap.putIfAbsent(labelId, new ArrayList<>());
            similarityMap.get(labelId).add(similarity);


        }

        Map<String, Float> averageSimilarityMap = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : similarityMap.entrySet()) {
            String labelId = entry.getKey();
            List<Float> similarities = entry.getValue();

            float sum = 0;
            for (Float similarity : similarities) {
                sum += similarity;
            }
            float average = sum / similarities.size();
            averageSimilarityMap.put(labelId, average);

            Log.e(TAG, "Average Similarity: " + average + " for Label ID: " + labelId);
        }

        String maxLabelId = "";
        float maxAverage = Float.MIN_VALUE;
        for (Map.Entry<String, Float> entry : averageSimilarityMap.entrySet()) {
            String labelId = entry.getKey();
            float average = entry.getValue();

            if (average > maxAverage) {
                maxAverage = average;
                maxLabelId = labelId;
            }
        }

        // Log the results (optional)
        Log.e(TAG, "Maximum Average Similarity: " + maxAverage + " for Label ID: " + maxLabelId);

        // Return the labelId with the highest average similarity
        return maxLabelId;
    }

    public float[] preprocessBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.postScale(-1, 1, width / 2f, height / 2f);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        float[] data = new float[105 * 105];
        int index = 0;

        //attempt to normalize correctly, i dont think its working right
        for (int i = 0; i < rotatedBitmap.getWidth(); i++) {
            for (int j = 0; j < rotatedBitmap.getHeight(); j++) {
                int pixel = rotatedBitmap.getPixel(i, j);

                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                float grayscale = (r + g + b) / 3.0f / 255.0f;
                data[index++] = grayscale;
            }
        }

        return data;
    }


    public void close() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
        } catch (OrtException e) {
            Log.e(TAG, "Error closing ONNX environment or session", e);
        }
    }

    public Pair<String, Map<String, Float>> classifyAndReturnPredAndSimilarityMap(Bitmap bitmap) {
        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems();

        Pair<String, Map<String, Float>> resultMap;

        Map<String, List<Float>> similarityMap = new HashMap<>();

        for (SupportSetItem item : supportSet) {
            String labelId = item.getLabelId();
            Bitmap bitmap2 = item.getBitmap();

            float[][] result = findSimilarity(bitmap, bitmap2);
            float similarity = result[0][0] + 100f;

            similarityMap.putIfAbsent(labelId, new ArrayList<>());
            similarityMap.get(labelId).add(similarity);


        }

        Map<String, Float> averageSimilarityMap = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : similarityMap.entrySet()) {
            String labelId = entry.getKey();
            List<Float> similarities = entry.getValue();

            float sum = 0;
            for (Float similarity : similarities) {
                sum += similarity;
            }
            float average = sum / similarities.size();
            averageSimilarityMap.put(labelId, average);

            Log.e(TAG, "Average Similarity: " + average + " for Label ID: " + labelId);
        }

        String maxLabelId = "";
        float maxAverage = Float.MIN_VALUE;
        for (Map.Entry<String, Float> entry : averageSimilarityMap.entrySet()) {
            String labelId = entry.getKey();
            float average = entry.getValue();

            if (average > maxAverage) {
                maxAverage = average;
                maxLabelId = labelId;
            }
        }

        // Log the results (optional)
        Log.e(TAG, "Maximum Average Similarity: " + maxAverage + " for Label ID: " + maxLabelId);

        resultMap = new Pair<>(maxLabelId, averageSimilarityMap);


        // Return the labelId with the highest average similarity
        return resultMap;
    }

}