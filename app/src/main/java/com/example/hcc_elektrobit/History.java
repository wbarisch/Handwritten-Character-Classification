package com.example.hcc_elektrobit;

import java.util.ArrayList;
import java.util.List;

public class History {

    private static volatile History INSTANCE = null;


    private List<HistoryItem> historyItems = new ArrayList<>();
    private History() {

    }

    public static History getInstance() {
        if(INSTANCE == null) {
            synchronized (History.class) {
                if (INSTANCE == null) {
                    INSTANCE = new History();
                }
            }
        }
        return INSTANCE;
    }

    public void addItem(HistoryItem _hi){
        historyItems.add(_hi);
    }

    public List<HistoryItem> getItems(){
        return historyItems;
    }
}
