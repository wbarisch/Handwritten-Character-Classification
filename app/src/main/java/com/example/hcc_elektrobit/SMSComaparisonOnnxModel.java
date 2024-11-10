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
import java.util.Collections;
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

    private static final String TAG = "SMSonnxModel_Comp";

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
        String modelFileName = "siamese_comparison_model_500.onnx";
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
    public float computeCosineSimilarity(float[] emb1, float[] emb2) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < emb1.length; i++) {
            dotProduct += emb1[i] * emb2[i];
            normA += emb1[i] * emb1[i];
            normB += emb2[i] * emb2[i];
        }

        return dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB) + 1e-10f); // Added small epsilon to prevent division by zero
    }
    public Pair<String, Map<String, Float>> classifyAndReturnPredAndSimilarityMap(Bitmap bitmap) {
        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems();
        Map<String, List<Float>> similarityMap = new HashMap<>();
        float[][] temp;
        if (!quantized) {
            temp = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bitmap);
        } else {
            temp = SMSQuantizedEmbeddingOnnxModel.getInstance().embedBitmap(bitmap);
        }
        float[] embeddingToCompare = temp[0];

        for (SupportSetItem item : supportSet) {
            String labelId = item.getLabelId();
            float[] supportEmbedding = item.getEmbeddingValues();
            if (supportEmbedding == null) {
                try {
                    supportEmbedding = ((float[][]) item.getImgEmbedding().getValue())[0];
                    item.setEmbeddingValues(supportEmbedding);
                } catch (OrtException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            float similarity = computeCosineSimilarity(embeddingToCompare, supportEmbedding);
            similarityMap.putIfAbsent(labelId, new ArrayList<>());
            similarityMap.get(labelId).add(similarity);
        }

        Map<String, Float> maxSimilarityMap = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : similarityMap.entrySet()) {
            String labelId = entry.getKey();
            List<Float> similarities = entry.getValue();
            float maxSimilarity = Collections.max(similarities);
            maxSimilarityMap.put(labelId, maxSimilarity);
            Log.e(TAG, "Maximum Similarity: " + maxSimilarity + " for Label ID: " + labelId);
        }
        String maxLabelId = "";
        float maxSimilarity = Float.NEGATIVE_INFINITY;
        for (Map.Entry<String, Float> entry : maxSimilarityMap.entrySet()) {
            String labelId = entry.getKey();
            float similarity = entry.getValue();
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                maxLabelId = labelId;
            }
        }
        Log.e(TAG, "Predicted Label: " + maxLabelId + " with Similarity: " + maxSimilarity);

        return new Pair<>(maxLabelId, maxSimilarityMap);
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