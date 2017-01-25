package com.gang.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import static com.gang.accessibility.ModuleConfig.DEBUG;
import static com.gang.accessibility.ModuleConfig.TAG;

/**
 * Created by Administrator on 2016/5/6.
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private AccessibilityTask mTask;
    private boolean isOpen = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        setServiceInfo();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(Statics.Key.COMMAND, -1);
        switch (command) {
            case Statics.START: {
                mTask = (AccessibilityTask) intent.getSerializableExtra("task_impl");
                if (mTask != null) {
                    isOpen = true;
                    setServiceInfo();
                    Toast.makeText(getApplicationContext(), "stated", Toast.LENGTH_SHORT).show();
                    if (DEBUG) {
                        Log.d(TAG, "onStartCommand: started , mTask " + mTask);
                    }
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "onStartCommand: start fialed , mTask is null ");
                    }
                }
                break;
            }
            case Statics.STOP: {
                isOpen = false;
                mTask = null;
                Toast.makeText(getApplicationContext(), "stoped", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (DEBUG) {
            Log.d("test_ad", "=====onAccessibilityEvent=====" + AccessibilityEvent.eventTypeToString(event.getEventType()) + ":" + event.getClassName());
        }
        if (isOpen && mTask != null) {
            mTask.onAccessibilityEvent(event);
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
        info.packageNames = new String[]{getPackageName(), "com.tencent.mm"};
        setServiceInfo(info);
    }
}
