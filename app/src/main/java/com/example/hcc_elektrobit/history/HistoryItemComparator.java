package com.example.hcc_elektrobit.history;

import java.util.Comparator;

public class HistoryItemComparator implements Comparator<HistoryItem> {

    @Override
    public int compare(HistoryItem o1, HistoryItem o2) {
        if (o1 == o2) return 0; // Same reference
        if (o1 == null) return -1; // Nulls are "smaller"
        if (o2 == null) return 1;

        // Compare by creation time
        int result = Long.compare(o2.getCreationTime(), o1.getCreationTime());

        if (result == 0) {
           result = Integer.compare(o2.hashCode(), o1.hashCode());
        }
        return result;
    }
}
