package com.example.hcc_elektrobit;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
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
    private Context context;
    private static final String TAG = "CNNonnxModel";

    public CNNonnxModel(Context context) {
        this.context = context;
        try {
            String modelPath = copyModelToCache("cnnModelMnist.onnx");
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            Log.e(TAG, "ONNX session created");
            Log.e("pog", "session created");
        } catch (OrtException e) {
            Log.e("CNNonnxModel", "Error creating ONNX session", e);
        } catch (IOException e) {
            Log.e("CNNonnxModel", "Error reading ONNX model from assets", e);
        }
    }

    private String copyModelToCache(String modelFileName) throws IOException {
        // Get the cache directory
        File cacheDir = context.getCacheDir();
        File modelFile = new File(cacheDir, modelFileName);

        // If the model file doesn't exist in the cache, copy it from assets
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
        return modelFile.getAbsolutePath(); // Return the absolute path of the model file
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
            // Return the classification result
            return output;

        } catch (OrtException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int classifyAndReturnDigit(Bitmap bitmap){
        float[][] result = this.classify(bitmap);
        //float[] result = {2.0f};
        return argmax(result[0]);

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

                // Extract RGB components
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Compute the grayscale value
                float grayscale = (r + g + b) / 3.0f / 255.0f;

                // Adjust grayscale to range between 1.0 and 0.9
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
            e.printStackTrace();
        }
    }
}