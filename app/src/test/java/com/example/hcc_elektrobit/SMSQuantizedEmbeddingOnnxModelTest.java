package com.example.hcc_elektrobit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;

public class SMSQuantizedEmbeddingOnnxModelTest {

    SMSQuantizedEmbeddingOnnxModel model = SMSQuantizedEmbeddingOnnxModel.getInstance();

    @Test
    public void testEmbedBitmap() {
        try {
            model.embedBitmap(null);
        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void testPreprocessBitmap() {
        try {
            model.preprocessBitmap(null);
        } catch (Exception e){
            fail();
        }
    }
}
