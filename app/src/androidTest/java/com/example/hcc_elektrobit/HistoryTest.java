package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HistoryTest {

    private History history;
    private Context context;
    private File bitmapDir;

    @Before
    public void setUp() {
        history = History.getInstance();
        context = ApplicationProvider.getApplicationContext();
        bitmapDir = new File(context.getFilesDir(), "saved_bitmaps");
        if (!bitmapDir.exists()) {
            bitmapDir.mkdirs();
        }
    }

    @After
    public void tearDown() {
        history.clearHistory(context);
    }

    @Test
    public void testAddItem() {
        HistoryItem item = new SMSHistoryItem(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), "TestPred", Map.of("testKey", 1.0f));
        history.addItem(item);

        List<HistoryItem> items = history.getItems();
        assertEquals(1, items.size());
        assertTrue(items.contains(item));
    }

    @Test
    public void testSaveItem_createsBitmapAndJson() throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        HistoryItem item = new CNNHistoryItem(bitmap, "TestPred", new float[][]{{1.0f}});

        history.saveItem(item, context);

        File[] files = bitmapDir.listFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);
    }

    @Test
    public void testUpdateHistory() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        CNNHistoryItem item = new CNNHistoryItem(bitmap, "TestPred", new float[][]{{1.0f}});

        history.saveItem(item, context);
        history.updateHistory(context);

        List<HistoryItem> items = history.getItems();
        assertFalse(items.isEmpty());
        assertTrue(items.get(0) instanceof CNNHistoryItem);
    }

    @Test
    public void testClearHistory() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        HistoryItem item = new SMSHistoryItem(bitmap, "TestPred", Map.of("testKey", 1.0f));
        history.addItem(item);
        history.saveItem(item, context);

        history.clearHistory(context);

        assertEquals(0, history.getItems().size());
        assertFalse(bitmapDir.exists());
    }

    @Test
    public void testGetSimilarityMapFromJSON() throws IOException, JSONException {
        File jsonFile = new File(bitmapDir, "model_outputs.json");
        try (FileOutputStream out = new FileOutputStream(jsonFile)) {
            out.write("{\"testFile.png\":{\"testKey\":1.0}}".getBytes());
        }

        Map<String, Float> resultMap = history.getSimilarityMapFromJSON(new File("testFile.png"), jsonFile);
        assertNotNull(resultMap);
        assertEquals(1, resultMap.size());
        assertEquals(1.0f, resultMap.get("testKey"), 0.01f);
    }

}
