package com.example.hcc_elektrobit;

import com.example.hcc_elektrobit.Event;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.util.*;


public class TrainingActivityViewModel extends AndroidViewModel {
    private static final String TAG = "TrainingViewModel";

    private final MutableLiveData<String> selectedCharacterLiveData = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedCharacterIdLiveData = new MutableLiveData<>(-1);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Void>> launchReviewActivityEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Void>> noImagesDialogEvent = new MutableLiveData<>();

    private final CharacterMapping characterMapping;
    private int bitmapSize = 28;
    private boolean saveAsWhiteCharacterOnBlack = true;
    private String selectedCharacter = "";
    private int selectedCharacterId = -1;

    public TrainingActivityViewModel(@NonNull Application application) {
        super(application);
        characterMapping = new CharacterMapping();
    }

    public LiveData<String> getSelectedCharacterLiveData() {
        return selectedCharacterLiveData;
    }

    public LiveData<Integer> getSelectedCharacterIdLiveData() {
        return selectedCharacterIdLiveData;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Event<Void>> getLaunchReviewActivityEvent() {
        return launchReviewActivityEvent;
    }

    public LiveData<Event<Void>> getNoImagesDialogEvent() {
        return noImagesDialogEvent;
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void resetSelectedCharacter() {
        selectedCharacter = "";
        selectedCharacterId = -1;
        selectedCharacterLiveData.setValue("");
        selectedCharacterIdLiveData.setValue(-1);
    }

    public void setBitmapSize(int size) {
        bitmapSize = size;
    }

    public int getBitmapSize() {
        return bitmapSize;
    }

    public void setSaveAsWhiteCharacterOnBlack(boolean value) {
        saveAsWhiteCharacterOnBlack = value;
    }

    public boolean isSaveAsWhiteCharacterOnBlack() {
        return saveAsWhiteCharacterOnBlack;
    }

    public void setSelectedCharacterId(String characterIdStr) {
        if (!characterIdStr.isEmpty()) {
            try {
                int id = Integer.parseInt(characterIdStr);
                selectedCharacterId = id;
                selectedCharacterIdLiveData.setValue(id);
                selectedCharacter = characterMapping.getCharacterForId(id);
                if (!selectedCharacter.isEmpty()) {
                    selectedCharacterLiveData.setValue(selectedCharacter);
                } else {
                    selectedCharacterLiveData.setValue("");
                    messageLiveData.setValue("Invalid character ID.");
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid number format for character ID.", e);
                selectedCharacterLiveData.setValue("");
                messageLiveData.setValue("Please enter a valid number.");
            }
        } else {
            selectedCharacterLiveData.setValue("");
            messageLiveData.setValue("Character ID cannot be empty.");
        }
    }

}