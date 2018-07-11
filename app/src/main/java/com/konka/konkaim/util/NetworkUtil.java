package com.konka.konkaim.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by HP on 2018-5-22.
 */

public class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        boolean available = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) available = true;
        return available;
    }
}
