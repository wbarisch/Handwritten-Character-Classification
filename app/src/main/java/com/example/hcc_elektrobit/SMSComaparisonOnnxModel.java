package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;


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

public class SMSComaparisonOnnxModel {
    private static SMSComaparisonOnnxModel INSTANCE = null;

    private OrtEnvironment env;
    private OrtSession session;

    private boolean quantized = false;

    private static final String TAG = "SMSonnxModel";

    private SMSComaparisonOnnxModel() {
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

    public static SMSComaparisonOnnxModel getInstance() {
        if (INSTANCE == null) {
            synchronized (SMSComaparisonOnnxModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SMSComaparisonOnnxModel();
                }
            }
        }
        return INSTANCE;
    }


    private String copyModelToCache() throws IOException {
        String modelFileName = "siamese_comparison_model_mine_245.onnx";
        File cacheDir = JFileProvider.getCacheDir();
        File modelFile = new File(cacheDir, modelFileName);

        if (!modelFile.exists()) {
            try (InputStream is = JFileProvider.getAssets().open(modelFileName);
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

    public void setQuantized(boolean quantized) {
        this.quantized = quantized;
    }
    public float[][] findSimilarityEmbeddings(OnnxTensor bitmap, OnnxTensor bitmap2) {
        try {

            OnnxTensor inputTensor1 = bitmap;
            OnnxTensor inputTensor2 = bitmap2;

            Map<String, OnnxTensor> inputMap = new HashMap<>();
            inputMap.put("embedding1", inputTensor1);  // Use the input name "input1" as defined in the model export
            inputMap.put("embedding2", inputTensor2);  // Use the input name "input2" as defined in the model export

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


    public Pair<String, Map<String, Float>> classifyAndReturnPredAndSimilarityMap(Bitmap bitmap) {
        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems();

        Pair<String, Map<String, Float>> resultMap;

        Map<String, List<Float>> similarityMap = new HashMap<>();
        float[][] temp;
        if (!quantized) {
             temp = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bitmap);
        }else{
            temp = SMSQuantizedEmbeddingOnnxModel.getInstance().embedBitmap(bitmap);
        }

        OnnxTensor tensorToCompare = loadTensor(temp);

        for (SupportSetItem item : supportSet) {
            String labelId = item.getLabelId();
            OnnxTensor tensorSupportItem = item.getImgEmbedding();

            float[][] result = findSimilarityEmbeddings(tensorToCompare, tensorSupportItem);
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



    public OnnxTensor loadTensor(float[][] emb) {
        OnnxTensor returnTensor;
        try {
            returnTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(emb[0]), new long[]{1, 4096});
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
        return returnTensor;
    }
}