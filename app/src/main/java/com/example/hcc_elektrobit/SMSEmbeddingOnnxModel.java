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
        String modelFileName = "siamese_embedding_model_mine_245.onnx";
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
            inputMap.put("input_image", inputTensor1);  // Use the input name "input1" as defined in the model export

            // Run the ONNX session with the inputs
            OrtSession.Result result = session.run(inputMap);

            // Get the output from the result
            float[][] output = (float[][]) result.get(0).getValue();

            // Log output information for debugging
            Log.i(TAG, "Output Tensor Shape: [" + output.length + ", " + output[0].length + "]");
            Log.i(TAG, "Output Tensor Values: " + java.util.Arrays.toString(output[0]));


            return output;

        } catch (OrtException e) {
            Log.e(TAG, "Error during classification", e);
        }
        return null;
    }



    public float[] preprocessBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
/**
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.postScale(-1, 1, width / 2f, height / 2f);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
**/
        Bitmap rotatedBitmap = bitmap;
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




}