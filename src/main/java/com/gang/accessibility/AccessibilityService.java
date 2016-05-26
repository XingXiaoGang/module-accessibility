package com.gang.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.gang.accessibility.impl.AccessibilityProxyDispatcher;

/**
 * Created by Administrator on 2016/5/6.
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private IAccessibilityProxy accessibilityProxy;
    private boolean isOpen = false;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Statics.ACCESSIBILITY_SERVER_ACTION);

        //要用application
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplication());
        localBroadcastManager.registerReceiver(new ActionReceiver(), filter);

        accessibilityProxy = new AccessibilityProxyDispatcher(this);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        setServiceInfo();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("test_ad", "=====onAccessibilityEvent=====" + AccessibilityEvent.eventTypeToString(event.getEventType()) + ":" + event.getClassName());
        if (isOpen) {
            if (accessibilityProxy != null) {
                accessibilityProxy.onAccessibilityEvent(event);
            } else {
                Log.d("test_ad", "=====onAccessibilityEvent:proxy is null=====");
            }
        } else {
            Log.d("test_ad", "=====onAccessibilityEvent:not open=====");
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void setServiceInfo() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 100;
        info.packageNames = new String[]{getPackageName(), "com.android.settings"};
        setServiceInfo(info);
    }

    private class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            int command = intent.getIntExtra(Statics.Key.COMMAND, -1);
            switch (command) {
                case Statics.START: {
                    isOpen = true;
                    setServiceInfo();
                    break;
                }
                case Statics.STOP: {
                    isOpen = false;
                    break;
                }
            }
        }
    }
}
