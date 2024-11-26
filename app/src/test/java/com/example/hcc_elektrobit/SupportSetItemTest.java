package com.example.hcc_elektrobit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupportSetItemTest {

    private SupportSetItem item;
    private String initialLabelId = "label1";
    private float[] initialEmbeddingValues = new float[] {0.1f, 0.2f, 0.3f};

    @Before
    public void setUp() {
        // Mock the bitmap and tensor-related dependencies if needed, otherwise, we focus on testable methods
        item = new SupportSetItem(null, initialLabelId);
        item.setEmbeddingValues(initialEmbeddingValues);
    }

    @Test
    public void testGetAndSetLabelId() {
        // Test initial label ID
        assertEquals("Initial label ID should be 'label1'", initialLabelId, item.getLabelId());

        // Set a new label ID and verify
        String newLabelId = "label2";
        item.setLabelId(newLabelId);
        assertEquals("Label ID should be updated to 'label2'", newLabelId, item.getLabelId());
    }

    @Test
    public void testGetAndSetEmbeddingValues() {
        // Test initial embedding values
        assertArrayEquals("Initial embedding values should match", initialEmbeddingValues, item.getEmbeddingValues(), 0.001f);

        // Set new embedding values and verify
        float[] newEmbeddingValues = new float[] {0.4f, 0.5f, 0.6f};
        item.setEmbeddingValues(newEmbeddingValues);
        assertArrayEquals("Embedding values should be updated", newEmbeddingValues, item.getEmbeddingValues(), 0.001f);
    }

    @Test
    public void testSetAndGetFileName() {
        // Set a filename and verify
        String filename = "sample_image.png";
        item.setFileName(filename);
        assertEquals("File name should match the set value", filename, item.getFileName());
    }
}
