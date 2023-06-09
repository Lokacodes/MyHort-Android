package com.lokacodes.kebunpintar;

import java.util.ArrayList;
import java.util.Arrays;

public class AddToArray {
    private String[] arrayNew;
    public AddToArray(String[] arr, String data) {
        ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(arr));
        arrayList.add(data);
        arr = arrayList.toArray(arr);
        arrayNew = arr;
    }

    public String[] getArrayNew() {
        return arrayNew;
    }
}
