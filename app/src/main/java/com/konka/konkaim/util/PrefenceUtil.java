package com.konka.konkaim.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.konka.konkaim.user.UserInfoUtil;

/**
 * Created by HP on 2018-5-25.
 */

public class PrefenceUtil {
    public static final String CURRENT_USER_STATE_FILENAME = "state_file";
    public static final String CURRENT_USER_STATE_FILENAME_KEY = "my_state";

    public static final String CURRENT_USER_LOGIN_INFO_FILENAME = "login_info_file";
    public static final String CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID = "my_accid";
    public static final String CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN = "my_token";

    public static final String CURRENT_USER_FRIEND_INFO_FILENAME = "friend_info_file";
    public static final String CURRENT_USER_FRIEND_INFO_FILENAME_KEY_ACCID = "friend_accid";

    public static void set(Context context, String fileName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String get(Context context, String fileName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void remove(Context context, String fileName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }
}
