package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class SMSonnxModel {
    private OrtEnvironment env;
    private OrtSession session;
    private final Context context;
    private static final String TAG = "SMSonnxModel";

    public SMSonnxModel(Context context) {
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

    private String copyModelToCache() throws IOException {
        String modelFileName = "siamese_model.onnx";
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
    public float[][] classify(Bitmap bitmap,Bitmap bitmap2) {
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
        float[][] result = this.classify(bitmap1,bitmap2);
        //float[] result = {2.0f};
        return result[0][0];

    }

    public float[] preprocessBitmap(Bitmap bitmap) {
        // Step 1: Resize the bitmap to 105x105
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 105, 105, true);

        // Step 2: Create a float array to hold the binary image data (1, 1, 105, 105)
        float[] inputTensorData = new float[1 * 1 * 105 * 105]; // total size is 1*1*105*105 = 11025

        // Step 3: Loop through each pixel, invert the colors, and normalize
        for (int i = 0; i < 105; i++) {
            for (int j = 0; j < 105; j++) {
                // Get the pixel value at (i, j)
                int pixel = resizedBitmap.getPixel(i, j);

                // Since the image is black and white, just grab the red channel (all channels are the same in grayscale)
                int red = Color.red(pixel);  // Extract the red channel, which is enough for a grayscale image

                // Invert the pixel (white becomes black and vice versa)
                float invertedValue = (255 - red) / 255.0f; // Normalizing to range [0, 1] and inverting

                // Assign the inverted and normalized pixel value to the float array
                inputTensorData[i * 105 + j] = invertedValue;
            }
        }

        // Return the preprocessed tensor data in the correct format
        return inputTensorData;
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