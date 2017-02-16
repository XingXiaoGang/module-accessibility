package com.gang.accessibility.tasks;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gang.accessibility.AccessibilityTask;

import java.util.List;

import static com.gang.accessibility.ModuleConfig.DEBUG;
import static com.gang.accessibility.ModuleConfig.TAG;

/**
 * Created by xingxiaogang on 2017/1/25.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AutoAllowTask extends AccessibilityTask implements Handler.Callback {


    private static final String[] TRIGGER_TEXT = new String[]{"允许", "同意", "确定", "确认", "是"};
    private static final int START_ID = 1001;
    private Handler mHandler;

    public AutoAllowTask() {
        mHandler = new Handler(this);
    }

    @Override
    protected void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(START_ID);
                }
                break;
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        int step = message.what;
        switch (step) {
            case START_ID: {
                if (DEBUG) {
                    Log.d(TAG, "handleMessage: 尝试点击 ");
                }
                for (String s : TRIGGER_TEXT) {
                    List<AccessibilityNodeInfo> infos = findNodeWithActionByText(s, AccessibilityNodeInfo.ACTION_CLICK);
                    if (infos != null) {
                        for (AccessibilityNodeInfo info : infos) {
                            if (TextUtils.equals(info.getText(), s)) {
                                if (performNodeAction(info, AccessibilityNodeInfo.ACTION_CLICK)) {
                                    if (DEBUG) {
                                        Log.d(TAG, "handleMessage: 点击成功");
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(START_ID, 1000);
                }
                break;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(START_ID);
        mHandler = null;
    }
}
