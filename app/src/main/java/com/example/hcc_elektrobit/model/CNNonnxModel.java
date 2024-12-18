package com.example.hcc_elektrobit.model;

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
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class CNNonnxModel {
    private OrtEnvironment env;
    private OrtSession session;
    private final Context context;

    private static CNNonnxModel INSTANCE = null;
    private static final String TAG = "CNNonnxModel";

    private CNNonnxModel(Context context) {
        this.context = context;
        try {
            String modelPath = copyModelToCache();
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            Log.i(TAG, "ONNX session created successfully.");
        } catch (OrtException e) {
            Log.e("CNNonnxModel", "Error creating ONNX session", e);
        } catch (IOException e) {
            Log.e("CNNonnxModel", "Error reading ONNX model from assets", e);
        }
    }

    public static CNNonnxModel getInstance(Context context) {
        if (INSTANCE == null) {


            INSTANCE = new CNNonnxModel(context.getApplicationContext());


        }
        return INSTANCE;
    }

    private String copyModelToCache() throws IOException {
        String modelFileName = "cnnModelMnist.onnx";
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
    public float[][] classify(Bitmap bitmap) {
        try {

            float[] inputTensorData = preprocessBitmap(bitmap);

            OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputTensorData), new long[]{1, 1, 28, 28});

            Map<String, OnnxTensor> inputMap = new HashMap<>();
            inputMap.put(session.getInputNames().iterator().next(), inputTensor);

            OrtSession.Result result = session.run(inputMap);
            float[][] output = (float[][]) result.get(0).getValue();

            Log.i(TAG, "Output Tensor Shape: [" + output.length + ", " + output[0].length + "]");

            Log.i(TAG, "Output Tensor Values: " + java.util.Arrays.toString(output[0]));


            //float [] output = {1.0f,2.9f,3.0f};
            return output;

        } catch (OrtException e) {
            Log.e(TAG, "Error during classification", e);
        }
        return null;
    }

    public int classifyAndReturnDigit(Bitmap bitmap){
        float[][] result = this.classify(bitmap);
        //float[] result = {2.0f};
        return argmax(result[0]);

    }

    public Pair<Integer, float[][]> classifyAndReturnIntAndTensor(Bitmap bitmap){
        float[][] result = this.classify(bitmap);
        return new Pair<>(argmax(result[0]), result);
    }

    public float[] preprocessBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.postScale(-1, 1, width / 2f, height / 2f);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        float[] data = new float[28 * 28];
        int index = 0;

        //attempt to normalize correctly, i dont think its working right
        for (int i = 0; i < rotatedBitmap.getWidth(); i++) {
            for (int j = 0; j < rotatedBitmap.getHeight(); j++) {
                int pixel = rotatedBitmap.getPixel(i, j);

                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                float grayscale = (r + g + b) / 3.0f / 255.0f;
                grayscale = 1.0f - grayscale;
                data[index++] = grayscale;
            }
        }

        return data;
    }

    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
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
}