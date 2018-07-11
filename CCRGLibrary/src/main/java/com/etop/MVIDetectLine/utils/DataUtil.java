package com.etop.MVIDetectLine.utils;

public class DataUtil {
    private static byte[] myData;

    public static byte[] getData() {
        return myData;
    }

    public static void setData(byte[] takeData){
        myData = takeData;
    }
}
