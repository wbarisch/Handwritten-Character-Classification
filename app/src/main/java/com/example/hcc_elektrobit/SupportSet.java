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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SupportSet {

    private static volatile SupportSet INSTANCE = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> evaluationFuture;
    private Set<SupportSetItem> SupportSetItems = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> upperCaseLetters = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> lowerCaseLetters = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> digits = new TreeSet<>(new SupportSetItemComparator());

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

    // NEW: categorized SupportSetItems getters.

    public List<SupportSetItem> getUpperCaseLetters() {
        return new ArrayList<>(upperCaseLetters);
    }

    public List<SupportSetItem> getLowerCaseLetters() {
        return new ArrayList<>(lowerCaseLetters);
    }

    public List<SupportSetItem> getDigits() {
        return new ArrayList<>(digits);
    }

    // NEW - END

    public void saveItem(SupportSetItem setItem) {
        File dir  = new File(JFileProvider.getInternalDir(), "support_set");
        if (!dir.exists()) {
            dir.mkdir();
        }
        int genId = setItem.bitmap.getGenerationId();

        String fileName = setItem.labelId + "_" + String.valueOf(genId) + ".png";
        File file = new File(dir, fileName);


        while (file.exists()) {
            genId++;
            fileName = setItem.labelId + "_" + genId + ".png";
            file = new File(dir, fileName);
        }

        setItem.setFileName(fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            setItem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("Bitmap Saved!", "Bitmap saved in " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addItem(setItem);
    }

    public void updateSet() {
        File bitmapDir = new File(JFileProvider.getInternalDir(), "support_set");
        if (!bitmapDir.exists()) {
            return;
        }

        //SupportSetItems.clear();
        evaluationFuture = executorService.submit(() -> {
            for (File file : Objects.requireNonNull(bitmapDir.listFiles())) {
                try (FileInputStream in = new FileInputStream(file)) {
                    String fileName = file.getName();
                    if (checkItemLoaded(fileName)) {
                        continue;
                    }
                    Log.i("File Loaded", fileName);
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    String labelId = file.getName().substring(0, file.getName().indexOf("_"));
                    SupportSetItem _hi = new SupportSetItem(bmp, labelId);
                    _hi.setFileName(fileName);
                    SupportSetItems.add(_hi);

                    // NEW: Add item to categorized sets
                    if(Character.isUpperCase(labelId.charAt(0))){
                        upperCaseLetters.add(_hi);
                    } else if (Character.isLowerCase(labelId.charAt(0))) {
                        lowerCaseLetters.add(_hi);
                    } else if (Character.isDigit(labelId.charAt(0))) {
                        digits.add(_hi);
                    }
                    // NEW-END

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
