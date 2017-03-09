package com.gang.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.gang.accessibility.utils.SharePref;

import org.saturn.daemon.KeepAliveHelper;

import static com.gang.accessibility.ModuleConfig.DEBUG;
import static com.gang.accessibility.ModuleConfig.TAG;

/**
 * Created by Administrator on 2016/5/6.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private AccessibilityTask mTask;
    private boolean isStart = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        setServiceInfo();
        start(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(Statics.Key.COMMAND, -1);
        switch (command) {
            case Statics.START: {
                start(intent);
                break;
            }
            case Statics.STOP: {
                isStart = false;
                if (mTask != null) {
                    mTask.onDestroy();
                }
                mTask = null;

                InnerService.stopDemon(this);

                Toast.makeText(getApplicationContext(), "stoped", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (isStart && mTask != null) {
            mTask.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setServiceInfo() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 100;
        info.packageNames = new String[]{getPackageName(), "com.tencent.mm", "com.android.settings"};
        setServiceInfo(info);
    }

    private void start(Intent intent) {
        if (SharePref.getBoolean(this, SharePref.KEY_IS_OPEN, false) && !isStart) {
            isStart = true;
            if (mTask == null) {
                String taskClazz = "";
                if (intent != null) {
                    taskClazz = intent.getStringExtra("task_impl");
                }
                if (TextUtils.isEmpty(taskClazz)) {
                    taskClazz = SharePref.getString(this, SharePref.KEY_CURRENT_TASK, "");
                }
                if (!TextUtils.isEmpty(taskClazz)) {
                    try {
                        Class clazz = Class.forName(taskClazz);
                        mTask = (AccessibilityTask) clazz.newInstance();
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, "onStartCommand: 反射失败：", e);
                        }
                    }

                    if (mTask != null) {
                        isStart = true;
                        mTask.setService(this);
                        setServiceInfo();

                        //保活
                        InnerService.showHideNotification(this);
                        KeepAliveHelper.getInstance().start(getApplicationContext(), InnerService.class.getName(), 22879);

                        Toast.makeText(getApplicationContext(), "stated", Toast.LENGTH_SHORT).show();
                        if (DEBUG) {
                            Log.d(TAG, "onStartCommand: started , mTask " + mTask);
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "onStartCommand: start fialed , mTask is null ");
                        }
                    }

                }
            }
        }
    }

}
