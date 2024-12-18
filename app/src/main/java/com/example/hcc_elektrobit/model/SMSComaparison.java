package com.example.hcc_elektrobit.model;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;


import com.example.hcc_elektrobit.support_set.SupportSet;
import com.example.hcc_elektrobit.support_set.SupportSetItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSComaparison {
    private static SMSComaparison INSTANCE = null;

    private boolean quantized = false;

    private static final String TAG = "SMSonnxModel_Comp";

    private SMSComaparison() {
    }

    public static SMSComaparison getInstance() {
        if (INSTANCE == null) {
            synchronized (SMSComaparison.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SMSComaparison();
                }
            }
        }
        return INSTANCE;
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

            float similarity = computeCosineSimilarity(embeddingToCompare, supportEmbedding);
            similarityMap.putIfAbsent(labelId, new ArrayList<>());
            similarityMap.get(labelId).add(similarity);
        }
        //Map<String, Float> averageSimilarityMap = new HashMap<>();
        Map<String, Float> maxSimilarityMap = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : similarityMap.entrySet()) {
            String labelId = entry.getKey();
            List<Float> similarities = entry.getValue();
            /*float sum = 0;
            for (Float similarity : similarities) {
                sum += similarity;
            }
            float average = sum / similarities.size();
            averageSimilarityMap.put(labelId, average);
            Log.e(TAG, "Average Similarity: " + average + " for Label ID: " + labelId); */
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

    // Overloaded method to handle input mode as a parameter
    public Pair<String, Map<String, Float>> classifyAndReturnPredAndSimilarityMap(Bitmap bitmap, int inputMode) {
        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems(inputMode);
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

    public float computeCosineSimilarityBitmap(Bitmap bm1, Bitmap bm2) {
        float[] emb1 = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bm1)[0];
        float[] emb2 = SMSEmbeddingOnnxModel.getInstance().embedBitmap(bm2)[0];

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

    public void close() {
    }

}