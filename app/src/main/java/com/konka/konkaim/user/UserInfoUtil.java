package com.konka.konkaim.user;

import android.content.Context;

import com.konka.konkaim.util.PrefenceUtil;

/**
 * Created by HP on 2018-5-8.
 */

public class UserInfoUtil {
    /**
     * accid : string
     * code : string
     * desc : string
     * token : string
     */

    public static String accid;
    public static String code;
    public static String desc;
    public static String token;

    public static String current_user_state;

    public static String getAccid() {
        return accid;
    }

    public static void setAccid(String _accid) {
        accid = _accid;
    }

    public static String getCode() {
        return code;
    }

    public static void setCode(String _code) {
        code = _code;
    }

    public static String getDesc() {
        return desc;
    }

    public static void setDesc(String _desc) {
        desc = _desc;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String _token) {
        token = _token;
    }

    public static void setCurrent_user_state(String current_user_state) {
        UserInfoUtil.current_user_state = current_user_state;
    }

    public static String getCurrent_user_state() {
        return current_user_state;
    }

    public static void clearUserInfo(){
        accid = null;
        code = null;
        token = null;
        desc = null;
    }

    public static boolean canAutoLogin(Context context) {
        final String accid = PrefenceUtil.get(context, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
        final String token = PrefenceUtil.get(context, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);
        System.out.println("doLogin-->accid=" + accid + ", token=" + token);
        if (accid != null && token != null) {
            return true;
        } else
            return false;
    }
}
