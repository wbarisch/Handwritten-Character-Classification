package com.example.hcc_elektrobit;

import android.graphics.Bitmap;


import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class CNNonnxModel {
    private OrtEnvironment env;
    private OrtSession session;
    String modelPath = "";

    public CNNonnxModel() {
        try {
            // Create the environment and session for ONNX model
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    public float[] classify(Bitmap bitmap) {
        return new float[0];
    }
}