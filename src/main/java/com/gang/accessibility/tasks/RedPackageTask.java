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


    private static final String[] TRIGGER_TEXT = new String[]{"[微信红包]"};
    //触发以后执行的步骤
    private static final String[][] STEPS = new String[][]{
            {"查看我的红包记录"},//第二步，已经领过
            {"领取红包"},//第二步,还没领过
            {"拆红包"}//第三步,还没领过
    };

    private Handler mHandler;
    private boolean isRedPackageComes;
    private int mLastTep = -1;

    public RedPackageTask() {
        mHandler = new Handler(this);
    }

    private int getCurrentStep(AccessibilityEvent event) {
        int step = -1;
        if (event != null && event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> strs = event.getText();
            if (strs != null) {
                out:
                for (CharSequence str : strs) {
                    for (String s : TRIGGER_TEXT) {
                        if (str.toString().contains(s)) {
                            step = 0;
                            break out;
                        }
                    }
                }
            }
        } else {
            AccessibilityNodeInfo root = getService().getRootInActiveWindow();
            if (root != null) {
                out:
                for (int i = 0; i < STEPS.length; i++) {
                    String[] targetText = STEPS[i];
                    for (String str : targetText) {
                        List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText(str);
                        for (AccessibilityNodeInfo info : infos) {
                            if (TextUtils.equals(info.getText(), str)) {
                                step = i + 1;//偏移一位
                                break out;
                            }
                        }
                    }
                }
            }
        }
        return step;
    }

    @Override
    protected void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        //判断事件类型
        int step = -1;
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                if (!isRedPackageComes) {
                    step = getCurrentStep(event);
                    if (step == 0) {
                        isRedPackageComes = true;
                    }
                } else {
                    step = getCurrentStep(event);
                }
                break;
            }
            default: {
                return;
            }
        }

        if (DEBUG) {
            Log.d(TAG, "onAccessibilityEvent: last step :" + mLastTep + " current step:" + step + " isRedPackageComes:" + isRedPackageComes);
        }

        mLastTep = step;

        //按步骤操作
        if (isRedPackageComes) {
            switch (step) {
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
                    getService().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    if (DEBUG) {
                        Log.d(TAG, "onAccessibilityEvent: 红包已领取过");
                        isRedPackageComes = false;
                    }
                    break;
                }
                case 2: {
                    mHandler.sendEmptyMessageDelayed(2, 100);
                    break;
                }
                case 3: {
                    isRedPackageComes = false;
                    boolean open = false;
                    if (DEBUG) {
                        Log.d(TAG, "onAccessibilityEvent: step 2 尝试拆红包 :");
                    }
                    List<AccessibilityNodeInfo> infos2 = findNodeWithActionByText("拆红包", AccessibilityNodeInfo.ACTION_CLICK);
                    for (AccessibilityNodeInfo info : infos2) {
                        while (info.getParent() != null && ((info.getActions() & AccessibilityNodeInfo.ACTION_CLICK) != AccessibilityNodeInfo.ACTION_CLICK)) {
                            info = info.getParent();
                        }
                        open = info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (open) {
                            if (DEBUG) {
                                Log.d(TAG, "onAccessibilityEvent: step 2 红包已拆 :" + info);
                            }
                            mHandler.sendEmptyMessageDelayed(4, 100);
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
            case 1: {
                break;
            }
            case 2: {
                //第一次点击
                boolean open = false;
                List<AccessibilityNodeInfo> infos = findNodeWithActionByText("领取红包", AccessibilityNodeInfo.ACTION_CLICK);
                if (DEBUG) {
                    Log.d(TAG, "onAccessibilityEvent: step 1 尝试领取 " + infos.size());
                }
                for (int size = infos.size() - 1; size >= 0; size--) {
                    open = performNodeAction(infos.get(size), AccessibilityNodeInfo.ACTION_CLICK);
                    if (open) {
                        if (DEBUG) {
                            Log.d(TAG, "onAccessibilityEvent: step 1 红包已领取 " + infos.get(size));
                        }
                        break;
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
