package com.konka.konkaim.util;

import android.util.Log;

/**
 * Created by HP on 2018-5-7.
 */

public class LogUtil {
    private static boolean DEBUG = true;

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public boolean getDEBUG() {
        return DEBUG;
    }

    public static void LogD(String TAG, String logStr) {
        if (DEBUG)
            Log.d(TAG, logStr);
    }

    public static void LogE(String TAG, String logStr) {
        if (DEBUG)
            Log.e(TAG, logStr);
    }

    public static void LogI(String TAG, String logStr) {
        if (DEBUG)
            Log.i(TAG, logStr);
    }
}
