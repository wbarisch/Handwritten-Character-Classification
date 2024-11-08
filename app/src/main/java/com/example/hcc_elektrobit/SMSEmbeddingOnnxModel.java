package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class SMSEmbeddingOnnxModel {
    private static SMSEmbeddingOnnxModel INSTANCE = null;

    private OrtEnvironment env;
    private OrtSession session;

    private static final String TAG = "SMSonnxModel";

    private SMSEmbeddingOnnxModel() {
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

    public static SMSEmbeddingOnnxModel getInstance() {
        if (INSTANCE == null) {
            synchronized (SMSEmbeddingOnnxModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SMSEmbeddingOnnxModel();
                }
            }
        }
        return INSTANCE;
    }

    private String copyModelToCache() throws IOException {
        String modelFileName = "embedding_model.onnx";
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
    public float[][] embedBitmap(Bitmap bitmap) {
        try {

            float[] input1TensorData = preprocessBitmap(bitmap);
            OnnxTensor inputTensor1 = OnnxTensor.createTensor(env, FloatBuffer.wrap(input1TensorData), new long[]{1, 1, 105, 105});

            Map<String, OnnxTensor> inputMap = new HashMap<>();
            inputMap.put("input_image", inputTensor1);
            OrtSession.Result result = session.run(inputMap);
            float[][] output = (float[][]) result.get(0).getValue();
            Log.i(TAG, "Output Tensor Shape: [" + output.length + ", " + output[0].length + "]");
            Log.i(TAG, "Output Tensor Values: " + java.util.Arrays.toString(output[0]));


            return output;

        } catch (OrtException e) {
            Log.e(TAG, "Error during classification", e);
        }
        return null;
    }



    public float[] preprocessBitmap(Bitmap bitmap) {
        int targetWidth = 105;
        int targetHeight = 105;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        float[] data = new float[targetWidth * targetHeight];
        int index = 0;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int pixel = scaledBitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                float grayscale = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f;

                data[index++] = grayscale;
            }
        }
        return data;
    }




}