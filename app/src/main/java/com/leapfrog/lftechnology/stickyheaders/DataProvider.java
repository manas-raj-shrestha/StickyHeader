package com.leapfrog.lftechnology.stickyheaders;

import com.leapfrog.lftechnology.stickyheaders.itemmodels.HeaderItem;
import com.leapfrog.lftechnology.stickyheaders.itemmodels.Item;

import java.util.ArrayList;

public class DataProvider {
    static ArrayList<Item> items = new ArrayList<>();
    static DataProvider dataProvider = null;

    public static DataProvider getInstance() {
        if (dataProvider == null) {
            dataProvider = new DataProvider();
            createData();
        }

        return dataProvider;
    }

    private static void createData() {
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0)
                items.add(new HeaderItem("Item at " + i, "Item Description at " + i));
            else
                items.add(new Item("Item at " + i, "Item Description at " + i));
        }
    }

    public ArrayList<Item> getDataList() {
        return items;
    }

}
