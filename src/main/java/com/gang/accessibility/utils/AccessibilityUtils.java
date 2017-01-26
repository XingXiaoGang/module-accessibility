package com.gang.accessibility.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.gang.accessibility.AccessibilityService;

import static com.gang.accessibility.ModuleConfig.DEBUG;
import static com.gang.accessibility.ModuleConfig.TAG;

/**
 * Created by xingxiaogang on 2017/1/25.
 */

public class AccessibilityUtils {

    public static final boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + AccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "isAccessibilitySettingsOn: ");
            }
        }
        if (DEBUG) {
            Log.d(TAG, "isAccessibilitySettingsOn:1 " + accessibilityEnabled);
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (DEBUG) {
                Log.d(TAG, "isAccessibilitySettingsOn: " + settingValue + "  ,, " + service);
            }
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
