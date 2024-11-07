package com.example.hcc_elektrobit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SupportSet {
    private static volatile SupportSet INSTANCE = null;

    private Set<SupportSetItem> SupportSetItems = new TreeSet<>(new Comparator<SupportSetItem>() {
        @Override
        public int compare(SupportSetItem o1, SupportSetItem o2) {
            int labelComparison = CharSequence.compare(o1.getLabelId(), o2.getLabelId());
            if (labelComparison != 0) {
                return labelComparison;
            }
            int generationComparison = Integer.compare(o1.getBitmap().getGenerationId(), o2.getBitmap().getGenerationId());
            if (generationComparison != 0) {
                return generationComparison;
            }
            return Integer.compare(o1.getBitmap().getByteCount(), o2.getBitmap().getByteCount());
        }
    });

    private SupportSet() {
    }

    public static SupportSet getInstance() {
        if (INSTANCE == null) {
            synchronized (SupportSet.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SupportSet();
                }
            }
        }
        return INSTANCE;
    }

    public void addItem(SupportSetItem _hi) {
        SupportSetItems.add(_hi);
    }

    public List<SupportSetItem> getItems() {
        return new ArrayList<>(SupportSetItems);
    }

    public void saveItem(SupportSetItem setItem) {
        File dir = new File(JFileProvider.getInternalDir(), "support_set");
        if (!dir.exists()) {
            dir.mkdir();

            int genId = setItem.getBitmap().getGenerationId();
            String fileName = setItem.getLabelId() + "_" + genId + ".png";
            File file = new File(dir, fileName);
            while (file.exists()) {
                genId++;
                fileName = setItem.getLabelId() + "_" + genId + ".png";
                file = new File(dir, fileName);
            }
            setItem.setFileName(fileName);

            try (FileOutputStream out = new FileOutputStream(file)) {
                setItem.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                Log.i("Bitmap Saved!", "Bitmap saved in " + file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            addItem(setItem);
        }
    }

    public void updateSet() {
        File bitmapDir = new File(JFileProvider.getInternalDir(), "support_set");
        if (!bitmapDir.exists()) {
            return;
        }
        SupportSetItems.clear();
        for (File file : Objects.requireNonNull(bitmapDir.listFiles())) {
            try (FileInputStream in = new FileInputStream(file)) {
                String fileName = file.getName();
                if (checkItemLoaded(fileName)) {
                    continue;
                }
                Log.i("File Loaded", fileName);
                Bitmap bmp = BitmapFactory.decodeStream(in);
                String labelId = fileName.substring(0, fileName.indexOf("_"));
                SupportSetItem item = new SupportSetItem(bmp, labelId);
                item.setFileName(fileName);
                SupportSetItems.add(item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean checkItemLoaded(String fileName) {
        for (SupportSetItem i : SupportSetItems) {
            if (Objects.equals(i.getFileName(), fileName)) {
                return true;
            }
        }
        return false;
    }

    public void clearSet() {
        File bitmapDir = new File(JFileProvider.getInternalDir(), "support_set");
        if (bitmapDir.exists()) {
            for (File file : Objects.requireNonNull(bitmapDir.listFiles())) {
                if (file.delete()) {
                    Log.i("File Deleted", "Deleted file: " + file.getName());
                } else {
                    Log.e("File Deletion Failed", "Failed to delete file: " + file.getName());
                }
            }
            SupportSetItems.clear();
            Log.i("History Cleared", "All history items have been deleted and memory cleared.");
        } else {
            Log.i("No History Found", "No history to delete.");
        }
    }

    public void removeItem(SupportSetItem item) {
        Log.i("SupportSetItems Before Removal", item.getFileName());

        boolean isRemoved = SupportSetItems.remove(item);
        if (isRemoved) {
            Log.i("Item Removal", "Item successfully removed from the SupportSetItems set.");
        } else {
            Log.e("Item Removal Failed", "Item was not found in the SupportSetItems set.");
        }

        File fileDir = new File(JFileProvider.getInternalDir(), "support_set");
        String fileName = item.getFileName();
        Log.i("File Name", fileName);
        File file = new File(fileDir, fileName);

        if (file.exists() && file.delete()) {
            Log.i("Image Deleted", "Deleted file: " + fileName);
        } else {
            Log.e("Image Deletion Failed", "Could not delete file: " + fileName);
        }
    }

    public void renameItem(SupportSetItem item, String newLabel) {
        File dir = new File(JFileProvider.getInternalDir(), "support_set");


        String fileName = item.getFileName();
        File file = new File(dir, fileName);
        item.setLabelId(newLabel);
        String newFileName = item.getLabelId() + fileName.substring(fileName.indexOf('_'));
        File newFile = new File(dir, newFileName);
        file.renameTo(newFile);
        item.setFileName(newFileName);


    }

}