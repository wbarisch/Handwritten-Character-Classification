package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;

public class SMSonnxModel {
    private static SMSonnxModel INSTANCE = null;

    private OrtEnvironment env;
    private OrtSession session;
    private final Context context;
    private static final String TAG = "SMSonnxModel";

    public float same_image_similarity = 0.9850878f;

    public int bmp_size = 105;

    private SMSonnxModel(Context context) {
        this.context = context;
        try {
            String modelPath = copyModelToCache();
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            Log.i(TAG, "ONNX session created successfully.");

            // Log input names and shapes
            Map<String, NodeInfo> inputInfoMap = session.getInputInfo();
            for (Map.Entry<String, NodeInfo> entry : inputInfoMap.entrySet()) {
                String inputName = entry.getKey();
                TensorInfo tensorInfo = (TensorInfo) entry.getValue().getInfo();
                Log.i(TAG, "Input Name: " + inputName + ", Shape: " + Arrays.toString(tensorInfo.getShape()));
            }

            // Log output names and shapes
            Map<String, NodeInfo> outputInfoMap = session.getOutputInfo();
            for (Map.Entry<String, NodeInfo> entry : outputInfoMap.entrySet()) {
                String outputName = entry.getKey();
                TensorInfo tensorInfo = (TensorInfo) entry.getValue().getInfo();
                Log.i(TAG, "Output Name: " + outputName + ", Shape: " + Arrays.toString(tensorInfo.getShape()));
            }

        } catch (OrtException e) {
            Log.e(TAG, "Error creating ONNX session", e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading ONNX model from assets", e);
        }
    }

    public static SMSonnxModel getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SMSonnxModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SMSonnxModel(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private String copyModelToCache() throws IOException {
        String modelFileName = "siamese_embedding_model_500.onnx";
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
    public float findSimilarity(Bitmap bitmap1, Bitmap bitmap2) {
        try {
            float[][][][] input1TensorData = preprocessBitmap(bitmap1);
            float[][][][] input2TensorData = preprocessBitmap(bitmap2);
            OnnxTensor inputTensor1 = OnnxTensor.createTensor(env, input1TensorData);
            OnnxTensor inputTensor2 = OnnxTensor.createTensor(env, input2TensorData);
            float[] embedding1 = getEmbedding(inputTensor1);
            float[] embedding2 = getEmbedding(inputTensor2);
            embedding1 = normalizeEmbedding(embedding1);
            embedding2 = normalizeEmbedding(embedding2);

            float similarity = computeCosineSimilarity(embedding1, embedding2);

            return similarity;

        } catch (OrtException e) {
            Log.e(TAG, "Error during classification", e);
        }
        return 0.0f;
    }

    private float[] normalizeEmbedding(float[] embedding) {
        float norm = 0.0f;
        for (float value : embedding) {
            norm += value * value;
        }
        norm = (float) Math.sqrt(norm) + 1e-10f;
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] /= norm;
        }
        return embedding;
    }



    private float[] getEmbedding(OnnxTensor inputTensor) throws OrtException {
        Map<String, OnnxTensor> inputMap = new HashMap<>();
        inputMap.put("input_image", inputTensor);

        // Run the model
        OrtSession.Result result = session.run(inputMap);

        // Get the embedding from the output
        float[][] embeddingOutput = (float[][]) result.get(0).getValue();
        float[] embedding = embeddingOutput[0];

        return embedding;
    }



    private float computeCosineSimilarity(float[] emb1, float[] emb2) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < emb1.length; i++) {
            dotProduct += emb1[i] * emb2[i];
            normA += emb1[i] * emb1[i];
            normB += emb2[i] * emb2[i];
        }

        return dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB) + 1e-10f);
    }

    public float classify_similarity(Bitmap bitmap1, Bitmap bitmap2){
        float similarity = this.findSimilarity(bitmap1, bitmap2);
        return similarity;
    }

    public String classify_id(Bitmap bitmap1){

        SupportSet.getInstance().updateSet();

        List<SupportSetItem> supportSet = SupportSet.getInstance().getItems();

        Map<String, List<Float>> similarityMap = new HashMap<>();

        for (SupportSetItem item : supportSet) {
            String labelId = item.getLabelId();
            Bitmap bitmap2 = item.getBitmap();

            float similarity = findSimilarity(bitmap1, bitmap2);

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
        Log.e(TAG, "Maximum Average Similarity: " + maxAverage + " for Label ID: " + maxLabelId);
        return maxLabelId;
    }

    public float[][][][] preprocessBitmap(Bitmap bitmap) {
        int targetWidth = 105;
        int targetHeight = 105;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        float[][][][] data = new float[1][1][targetHeight][targetWidth];

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int pixel = scaledBitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                float grayscale = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f;
                data[0][0][y][x] = grayscale;
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

    public float get_same_image_similarity() {
        return same_image_similarity;
    }
}