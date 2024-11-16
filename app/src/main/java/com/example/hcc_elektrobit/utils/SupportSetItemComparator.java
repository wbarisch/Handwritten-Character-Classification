package com.example.hcc_elektrobit.utils;

import com.example.hcc_elektrobit.support_set.SupportSetItem;

import java.io.Serializable;
import java.util.Comparator;

public class SupportSetItemComparator implements Comparator<SupportSetItem>, Serializable {
    private static final long serialVersionUID = 1L;

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

}
