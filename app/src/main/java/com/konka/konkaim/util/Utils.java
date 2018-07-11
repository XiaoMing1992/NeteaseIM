package com.konka.konkaim.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HP on 2018-5-7.
 */

public class Utils {

    /**
     * 正则表达式判断手机号
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(final String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 判断内容是否只是为数字或者字母
     *
     * @param content
     * @return
     */
    public static boolean isDigitOrLetter(final String content) {
        if (content == null || content.isEmpty()) return false;
        for (int i = 0; i < content.length(); i++) {
            if (!((content.charAt(i) >= 'a' && content.charAt(i) <= 'z')
                    || (content.charAt(i) >= 'A' && content.charAt(i) <= 'Z')
                    || (content.charAt(i) >= '0' && content.charAt(i) <= '9'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 发送验证码
     *
     * @param mobile
     * @return
     */
    public static String sendSmsCode(final String mobile) {
        String smsCode = "";

        return smsCode;
    }

    public static int screenWidth;
    public static int screenHeight;

    public static void computeScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static String getVersionName(Context context) {
        String versionName = "1.0.0";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "v" + versionName;
    }

    /**
     * 计算包含中文的字符串的长度
     *
     * @param value
     * @return
     */
    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; value != null && i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /**
     * 计算包含中文的字符串的长度
     *
     * @param value
     * @return
     */
    public static String getStrByLength(String value, int len) {
        int valueLength = 0;
        String str = "";
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; value != null && i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
            str += temp;
            //System.out.println("str="+str);
            if (len <= valueLength) {
                break;
            }
        }
        return str;
    }
}
