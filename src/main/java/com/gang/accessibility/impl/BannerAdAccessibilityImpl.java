package com.gang.accessibility.impl;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gang.accessibility.AccessibilityService;

import static com.gang.accessibility.Statics.TAG;


/**
 * Created by Administrator on 2016/5/6.
 */
class BannerAdAccessibilityImpl extends BaseAccessibilityProxy {

    private static final String TASK_CLICK_ADS = "click_ads";

    BannerAdAccessibilityImpl(AccessibilityService service) {
        super(service);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                break;
            }
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                handleTask(event);
                break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean handleTask(AccessibilityEvent event) {
        boolean res = false;
        if (event == null || "辅助点击".equals(event.getSource().getText().toString())) {
            AccessibilityNodeInfo nodeInfo = findNodeById(getViewId("bannerContainer"), -3

            );
            res = nodeInfo != null;
            performNodeAction(nodeInfo, AccessibilityNodeInfo.ACTION_CLICK);
            if (res) {
                finishAccessibility();
                Log.d(TAG, "handleTask success;");
            } else {
                Log.d(TAG, "handleTask failed: retrying...");
                retryTask(500, TASK_CLICK_ADS);
            }
        }
        return res;
    }

    @Override
    protected boolean onRetryTask(String id) {
        boolean success = false;
        switch (id) {
            case TASK_CLICK_ADS: {
                success = handleTask(null);
                break;
            }
        }
        return success;
    }
}
