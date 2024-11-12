package com.example.hcc_elektrobit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import org.junit.Test;

import java.util.List;

public class SupportSetTest {

    public static SupportSet supset = SupportSet.getInstance();

    @Test
    public void supportSetItemSetGetTest() {

        SupportSetItem supsetItem = new SupportSetItem(null, "t");

        supset.addItem(supsetItem);

        List<SupportSetItem> itemsRet = supset.getItems();

        assertNotNull(itemsRet);

    }


    @Test
    public void itemInputModeTest() {

        SupportSetItem supsetItemLower = new SupportSetItem(null, "t");
        SupportSetItem supsetItemUpper = new SupportSetItem(null, "t");
        SupportSetItem supsetItemNumber = new SupportSetItem(null, "t");

        supset.addItem(supsetItemLower);
        supset.addItem(supsetItemNumber);
        supset.addItem(supsetItemUpper);

        List<SupportSetItem> itemsRetUpper = supset.getItems(InputMode.UPPERCASE);
        List<SupportSetItem> itemsRetNumber = supset.getItems(InputMode.NUMBER);
        List<SupportSetItem> itemsRetLower = supset.getItems(InputMode.LOWERCASE);

        assertNotNull(itemsRetUpper);
        assertNotNull(itemsRetNumber);
        assertNotNull(itemsRetLower);

    }

}
