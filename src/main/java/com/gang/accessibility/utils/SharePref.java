package com.gang.accessibility.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xingxiaogang on 2017/1/25.
 */

public class SharePref {

    public static final String FILE_NAME = "share_p";

    public static final String KEY_IS_OPEN = "key_is_open";
    public static final String KEY_CURRENT_TASK = "key_current_task";

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }
}
