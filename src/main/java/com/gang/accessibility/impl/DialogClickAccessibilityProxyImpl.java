package com.gang.accessibility.impl;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gang.accessibility.AccessibilityService;

/**
 * Created by gang on 16-12-10.
 */
public class DialogClickAccessibilityProxyImpl extends BaseAccessibilityProxy implements Handler.Callback {

    private static final boolean DEBUG = true;
    private static final String TAG = "test_access";

    private String[] inAcceptBtnText = new String[]{"同意", "OK"};
    private static final int MSG_START = 100090;
    private Handler mHandler;

    public DialogClickAccessibilityProxyImpl(AccessibilityService service) {
        super(service);
        mHandler = new Handler(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //点击确认
        mHandler.removeMessages(MSG_START);
        mHandler.sendEmptyMessage(MSG_START);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START: {
                doActionClick();
                mHandler.sendEmptyMessageDelayed(MSG_START, 1000);
                break;
            }
        }
        return false;
    }

    private boolean doActionClick() {
        Log.i(TAG, "HandleNodeActionTask doActionClick:");

        final String[] nodeTexts = inAcceptBtnText;
        for (String txt : nodeTexts) {
            AccessibilityNodeInfo nodeInfo = findNodeByText(txt, 0);
            if (nodeInfo != null) {
                boolean res = intelligentClickNode(nodeInfo);
                if (DEBUG) {
                    Log.i(TAG, "HandleNodeActionTask Click:" + nodeInfo.getText().toString() + " res " + res);
                }
                return res;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        mHandler.removeMessages(MSG_START);
    }
}
