package com.gang.accessibility.tasks;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
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
public class RedPackageTask extends AccessibilityTask implements Handler.Callback {

    /**
     * 抢红包步骤：
     * 1.触发，进入；
     * 2.点击领取红包；
     * 3.拆红包；
     **/
    private static final String[] TRIGGER_TEXT = new String[]{"[微信红包]"};
    private static final String[] HAS_OPENED_FEATURE = new String[]{"查看我的红包记录"};
    private static final String[] NOT_OPENED_FEATURE = new String[]{"领取红包"};
    private static final String[] TO_OPENE_FEATURE = new String[]{"拆红包", "发了一个红包"};

    private Handler mHandler;
    private boolean isRedPackageComes;
    private int mCurrentStep = -1;

    private static final int MSG_TO_RECEIVE = 2;

    public RedPackageTask() {
        mHandler = new Handler(this);
    }

    private boolean isRedPackageComes(AccessibilityEvent event) {
        if (event != null && event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> strs = event.getText();
            if (strs != null) {
                for (CharSequence str : strs) {
                    for (String s : TRIGGER_TEXT) {
                        if (str.toString().contains(s)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isRedPackageOpened() {
        AccessibilityNodeInfo root = getService().getRootInActiveWindow();
        if (root != null) {
            final String[] hasOpenedFeature = HAS_OPENED_FEATURE;
            for (String str : hasOpenedFeature) {
                List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText(str);
                for (AccessibilityNodeInfo info : infos) {
                    if (TextUtils.equals(info.getText(), str)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void finishTask() {
        isRedPackageComes = false;
        mCurrentStep = -1;
    }

    @Override
    protected void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        //判断事件类型
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                if (!isRedPackageComes && isRedPackageComes(event)) {
                    isRedPackageComes = true;
                    mCurrentStep = 0;
                }
                break;
            }
            default: {
                return;
            }
        }

        if (DEBUG) {
            Log.d(TAG, "onAccessibilityEvent: current step:" + mCurrentStep + " isRedPackageComes:" + isRedPackageComes);
        }

        //按步骤操作
        if (isRedPackageComes) {
            switch (mCurrentStep) {
                case 0: {
                    //跳转
                    if (DEBUG) {
                        Log.d(TAG, "onAccessibilityEvent: step 0 尝试打开通知 ");
                    }
                    try {
                        Notification notification = (Notification) event.getParcelableData();
                        if (notification != null) {
                            PendingIntent pendingIntent = notification.contentIntent;
                            if (pendingIntent != null) {
                                pendingIntent.send();
                                mCurrentStep = 1;
                                if (DEBUG) {
                                    Log.d(TAG, "onAccessibilityEvent: step 0 打开通知 ");
                                }
                            }
                        }
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case 1: {
                    if (isRedPackageOpened()) {
                        getService().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        finishTask();
                        if (DEBUG) {
                            Log.d(TAG, "onAccessibilityEvent: 红包已领取过");
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_TO_RECEIVE, 100);
                    }
                    break;
                }
                case 3: {
                    boolean open = false;
                    if (DEBUG) {
                        Log.d(TAG, "onAccessibilityEvent: step 2 尝试拆红包 :");
                    }
                    String[] texts = TO_OPENE_FEATURE;
                    for (String text : texts) {
                        List<AccessibilityNodeInfo> infos2 = findNodeWithActionByText(text, AccessibilityNodeInfo.ACTION_CLICK);
                        if (DEBUG) {
                            Log.d(TAG, "handleMessage:搜索文本：" + text + " 搜索结果：" + infos2.size());
                        }
                        for (AccessibilityNodeInfo info : infos2) {
                            while (info.getParent() != null && ((info.getActions() & AccessibilityNodeInfo.ACTION_CLICK) != AccessibilityNodeInfo.ACTION_CLICK)) {
                                info = info.getParent();
                            }
                            open = info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                        if (open) {
                            if (DEBUG) {
                                Log.d(TAG, "onAccessibilityEvent: step 2 红包已拆 :");
                            }
                            finishTask();
                            mHandler.sendEmptyMessageDelayed(4, 100);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        int step = message.what;
        switch (step) {
            case MSG_TO_RECEIVE: {
                //第一次点击
                boolean open = false;
                final String[] receiveFeature = NOT_OPENED_FEATURE;
                out:
                for (String s : receiveFeature) {
                    List<AccessibilityNodeInfo> infos = findNodeWithActionByText(s, AccessibilityNodeInfo.ACTION_CLICK);
                    if (DEBUG) {
                        Log.d(TAG, "onAccessibilityEvent: step 1 尝试领取 " + infos.size());
                    }
                    for (int size = infos.size() - 1; size >= 0; size--) {
                        open = performNodeAction(infos.get(size), AccessibilityNodeInfo.ACTION_CLICK);
                        if (open) {
                            if (DEBUG) {
                                Log.d(TAG, "onAccessibilityEvent: step 1 红包已领取 " + infos.get(size));
                            }
                            mCurrentStep = 3;
                            break out;
                        }
                    }
                }
                break;
            }
            case 4: {
                getService().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                mHandler.sendEmptyMessageDelayed(5, 500);
                break;
            }
            case 5: {
                getService().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                break;
            }
        }
        return false;
    }
}
