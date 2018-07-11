package com.etop.MVIDetectLine.utils;

import android.os.Environment;

public class ConstantUtil {
    //public static final String UserID = "7332DBAFD2FD18301EF6";
    public static final String PATH = Environment.getExternalStorageDirectory() + "/Alpha/other/";
    public static final String INTENT_MOTOR_CONFIG = "motorConfig";

    private static String UserId = "";
    public static String getUserId() {
        return UserId;
    }
    public static void setUserId(String userId) {
        UserId = userId;
    }
}
