package com.example.hcc_elektrobit.support_set;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.hcc_elektrobit.shared.JFileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SupportSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static volatile SupportSet INSTANCE = null;
    private static transient ExecutorService executorService = Executors.newSingleThreadExecutor();
    private transient Future<?> evaluationFuture;
    private Set<SupportSetItem> SupportSetItems = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> upperCaseLetters = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> lowerCaseLetters = new TreeSet<>(new SupportSetItemComparator());
    private Set<SupportSetItem> digits = new TreeSet<>(new SupportSetItemComparator());
    private static boolean supportSetLoaded = false;

    private SupportSet() {
        initializeSupportSetDirectory();
    }

    public static SupportSet getInstance() {
        if (INSTANCE == null) {
            synchronized (SupportSet.class) {
                if(!supportSetLoaded){
                    loadSupportSet();
                }
                if (INSTANCE == null && !supportSetLoaded) {

                    INSTANCE = new SupportSet();
                }
            }
        }
        return INSTANCE;
    }

    private void initializeSupportSetDirectory() {
        File supportSetDir = new File(JFileProvider.getInternalDir(), "support_set");

        if (!supportSetDir.exists()) {
            supportSetDir.mkdir();
            copyAssetsToInternal("support_set");
            Log.i("Initialization", "Copied support_set folder from assets to internal storage.");
        } else {
            Log.i("Initialization", "support_set folder already exists in internal storage.");
        }
    }

    private void copyAssetsToInternal(String folderName) {
        AssetManager assetManager = JFileProvider.getAssets();
        try {
            String[] files = assetManager.list(folderName);
            if (files != null) {
                for (String fileName : files) {
                    try (InputStream in = assetManager.open(folderName + "/" + fileName);
                         FileOutputStream out = new FileOutputStream(new File(JFileProvider.getInternalDir(), folderName + "/" + fileName))) {

                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    Log.i("File Copied", "Copied " + fileName + " to internal storage.");
                }
            }
        } catch (IOException e) {
            Log.e("Asset Copy Error", "Error copying assets to internal storage", e);
        }
    }


    public void addItem(SupportSetItem _hi) {
        SupportSetItems.add(_hi);
    }

    public List<SupportSetItem> getItems() {
        return new ArrayList<>(SupportSetItems);
    }

    // Get a list of SupportSetItems for the specified input mode
    public List<SupportSetItem> getItems(int inputMode){

        switch (inputMode){
            case InputMode.UPPERCASE:
                return new ArrayList<>(upperCaseLetters);
            case InputMode.LOWERCASE:
                return new ArrayList<>(lowerCaseLetters);
            case InputMode.NUMBER:
                return new ArrayList<>(digits);
            default:
                return new ArrayList<>(SupportSetItems);
        }
    }

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
        saveSupportSet();
    }

    public void saveSupportSet() {
        File file = new File(JFileProvider.getInternalDir(), "support_set_serialized");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
            Log.i("SupportSet Serialization", "SupportSet serialized successfully");
        } catch (IOException e) {
            Log.e("Serialization Error", "Failed to serialize SupportSet", e);
        }
    }

    public static void loadSupportSet() {
        File file = new File(JFileProvider.getInternalDir(), "support_set_serialized");
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                INSTANCE = (SupportSet) in.readObject();
                executorService = Executors.newSingleThreadExecutor();
                Log.i("SupportSet Load", "SupportSet loaded from serialized file.");
                supportSetLoaded = true;
            } catch (IOException | ClassNotFoundException e) {
                Log.e("Deserialization Error", "Failed to load SupportSet", e);
            }
        }
    }

    public void updateSet() {
        File bitmapDir = new File(JFileProvider.getInternalDir(), "support_set");
        if (!bitmapDir.exists()) {
            return;
        }

        evaluationFuture = executorService.submit(() -> {
            if(supportSetLoaded){
                for (SupportSetItem i : SupportSetItems) {
                    i.loadBitmap();
                }
            return;
            }

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
            saveSupportSet();
        });
    }
    public boolean imagesLoaded() {
        if (evaluationFuture == null) {
            return false;
        }
        return evaluationFuture.isDone();
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
        saveSupportSet();
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

        saveSupportSet();

    }
}
