package com.gang.accessibility.impl;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gang.accessibility.AccessibilityService;


/**
 * Created by xingxiaogang on 2016/6/6.
 * 执行辅助任务(主动),适配更多机型只需要匹配的数组里完善文案即可
 */
public class GPAccessibilityProxyImpl extends BaseAccessibilityProxy {

    private static final boolean DEBUG = true;
    private static final String TAG = "test_access";
    private HandleNodeActionTask mClickInstallRunnable;//安装
    private HandleNodeActionTask mClickAcceptRunnable;//授权


    private String[] installBtnId = new String[]{"com.android.vending:id/buy_button"};
    private String[] inAcceptBtnId = new String[]{"com.android.vending:id/continue_button"};
    private String[] inInstallBtnText = new String[]{"安装", "Install"};
    private String[] inAcceptBtnText = new String[]{"确认", "OK"};
    private boolean isInstallClicked;
    private boolean isAcceptClicked;

    private boolean isRunning = false;

    public GPAccessibilityProxyImpl(AccessibilityService service) {
        super(service);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (!isRunning) {
            isRunning = true;
            //点击安装
            if (!isInstallClicked) {
                if (mClickInstallRunnable != null) {
                    isInstallClicked = mClickInstallRunnable.isSuccess();
                    mClickInstallRunnable.interrupt();
                    mClickInstallRunnable = null;
                }
                mClickInstallRunnable = new HandleNodeActionTask(installBtnId, inInstallBtnText, AccessibilityNodeInfo.ACTION_CLICK);
                mClickInstallRunnable.start();
            }

            //点击授权
            if (!isAcceptClicked) {
                if (mClickAcceptRunnable != null) {
                    isAcceptClicked = mClickAcceptRunnable.isSuccess();
                    mClickAcceptRunnable.interrupt();
                    mClickAcceptRunnable = null;
                }
                mClickAcceptRunnable = new HandleNodeActionTask(inAcceptBtnId, inAcceptBtnText, AccessibilityNodeInfo.ACTION_CLICK);
                mClickAcceptRunnable.start();
            } else {
                //所有任务完成
            }
        }
    }

    //执行核心
    private class HandleNodeActionTask extends Thread {

        private String[] nodeIds;
        private String[] nodeTexts;
        private int action;
        private boolean isSuccess;

        /**
         * @param ids    要匹配的id
         * @param texts  要匹配的文案
         * @param action 要执行的动作
         **/
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public HandleNodeActionTask(String[] ids, String[] texts, int action) {
            this.nodeIds = ids;
            this.nodeTexts = texts;
            this.action = action;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        @Override
        public void run() {
            int times = 50;
            boolean runFlag = true;
            while (runFlag) {
                if (nodeIds != null) {
                    out:
                    for (String id : nodeIds) {
                        AccessibilityNodeInfo nodeInfo = findNodeById(id, 0);
                        if (nodeInfo != null) {
                            switch (action) {
                                case AccessibilityNodeInfo.ACTION_CLICK: {
                                    boolean res = intelligentClickNode(nodeInfo);
                                    if (DEBUG) {
                                        Log.i(TAG, "HandleNodeActionTask Click:" + nodeInfo.getText().toString() + " res " + res);
                                    }
                                    if (res) {
                                        isSuccess = true;
                                        runFlag = false;
                                    }
                                    break out;
                                }
                            }
                        }
                    }
                }

                if (!isSuccess && nodeTexts != null) {
                    out:
                    for (String txt : nodeTexts) {
                        AccessibilityNodeInfo nodeInfo = findNodeByText(txt, 0);
                        if (nodeInfo != null) {
                            switch (action) {
                                case AccessibilityNodeInfo.ACTION_CLICK: {
                                    boolean res = intelligentClickNode(nodeInfo);
                                    if (DEBUG) {
                                        Log.i(TAG, "HandleNodeActionTask Click:" + nodeInfo.getText().toString() + " res " + res);
                                    }
                                    if (res) {
                                        isSuccess = true;
                                        runFlag = false;
                                    }
                                    break out;
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                times--;
                if (times == 0) {
                    runFlag = false;
                }
            }
        }
    }
}
