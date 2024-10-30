package com.example.hcc_elektrobit;

import androidx.lifecycle.ViewModel;
import java.util.List;

public class SupportSetViewModel extends ViewModel {

    private final SupportSet supportSet = SupportSet.getInstance();

    public List<SupportSetItem> getItems() {
        return supportSet.getItems();
    }

    public void updateSet() {
        supportSet.updateSet();
    }

    public void saveItem(SupportSetItem item) {
        supportSet.saveItem(item);
    }

    public void clearSet() {
        supportSet.clearSet();
    }

    public void removeItem(SupportSetItem item) {
        supportSet.removeItem(item);
    }

    public void renameItem(SupportSetItem item, String newLabel) {
        SupportSetItem updatedItem = new SupportSetItem(item.getBitmap(), newLabel);
        updatedItem.setFileName(item.getFileName());
        saveItem(updatedItem);
        removeItem(item);
    }
}
