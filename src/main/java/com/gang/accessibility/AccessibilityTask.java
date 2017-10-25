package com.gang.accessibility;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.gang.accessibility.ModuleConfig.TAG;

/**
 * Created by xingxiaogang on 2017/1/25.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class AccessibilityTask implements Serializable {
    private AccessibilityService service;

    protected final AccessibilityService getService() {
        return service;
    }

    protected final void setService(AccessibilityService service) {
        this.service = service;
    }

    /**
     * 事件来源
     **/
    protected abstract void onAccessibilityEvent(AccessibilityEvent event);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected final AccessibilityNodeInfo getRootNode() {
        return service.getRootInActiveWindow();
    }

    protected final List<AccessibilityNodeInfo> findNodeWithActionByText(String text, int action) {
        final AccessibilityNodeInfo root = getRootNode();
        List<AccessibilityNodeInfo> res = new ArrayList<>();
        if (root != null) {
            final int maxDeepth = 3;
            List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByText(text);
            for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                if (ModuleConfig.DEBUG) {
                    Log.d(TAG, "findNodeWithActionByText: 找到：" + nodeInfo);
                }
                if ((nodeInfo.getActions() & action) == action) {
                    res.add(nodeInfo);
                } else {
                    int deepth = 0;
                    while (deepth < maxDeepth && (nodeInfo.getActions() & action) != action && nodeInfo.getParent() != null) {
                        nodeInfo = nodeInfo.getParent();
                        if (nodeInfo != null) {
                            if ((nodeInfo.getActions() & action) == action) {
                                res.add(nodeInfo);
                            } else if (nodeInfo.getChildCount() > 0) {
                                for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                                    AccessibilityNodeInfo note = nodeInfo.getChild(i);
                                    if ((note.getActions() & action) == action) {
                                        res.add(note);
                                    }
                                }
                            }
                            break;
                        }
                        deepth++;
                    }
                }
            }
        }
        return res;
    }

    protected final List<AccessibilityNodeInfo> findNodeWithClassByRect(String className, Rect area, int action) {
        final AccessibilityNodeInfo root = getRootNode();
        List<AccessibilityNodeInfo> res = new ArrayList<>();
        if (root != null) {
            final int maxDeepth = 3;
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo node = root.getChild(i);
                if (node != null) {
                    if (matchFeatures(node, className, area, action)) {
                        res.add(node);
                    } else {
                        for (int i1 = 0; i1 < node.getChildCount(); i1++) {

                        }
                    }
                }
            }
        }
        return res;
    }

    protected final boolean matchFeatures(AccessibilityNodeInfo node, String className, Rect area, int action) {
        Rect tempRect = new Rect();
        if (node != null) {
            if (node.getClassName().toString().contains(className) && (node.getActions() & action) == action) {
                node.getBoundsInScreen(tempRect);
                if (area.contains(tempRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected final AccessibilityNodeInfo findNodeWithActionById(String id, int action) {
        AccessibilityNodeInfo accessibilityNodeInfo = null;
        final AccessibilityNodeInfo root = getRootNode();
        if (root != null) {
            List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByViewId(id);
            for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                if ((nodeInfo.getActions() & action) == action) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    protected final String getViewId(String idstr) {
        return service.getPackageName() + ":id/" + idstr;
    }

    protected final boolean performNodeAction(AccessibilityNodeInfo nodeInfo, int action) {
        return nodeInfo != null && nodeInfo.performAction(action);
    }

    public static final void printChilds(AccessibilityNodeInfo root) {
        if (root != null) {
            Log.d(TAG, "====root start======name:" + root.getClassName() + ",child:" + root.getChildCount() + "text:[" + root.getText() + "]" + "actions:[" + root.getActions() + "]");
            int childCount = root.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = root.getChild(i);
                    printChilds(child);
                }
            } else {
                Log.d(TAG, "====root end======name:" + root.getClassName() + ",child:" + root.getChildCount());
            }
        } else {
            Log.d(TAG, "====root is null=======");
        }
    }

    //任务重试
    protected final void retryTask(long timeDelay, String id) {
        retryTask(timeDelay, id, 1);
    }

    //任务重试
    protected final void retryTask(long timeDelay, String id, int tryTimes) {
        if (id == null) {
            throw new RuntimeException("id can't be null");
        }
        if (tryTimes < 1) {
            throw new RuntimeException("tryTimes should >=1");
        }
        Message message = mHandler.obtainMessage(R.id.retry_task, id);
        message.arg1 = tryTimes;
        message.arg2 = (int) timeDelay;
        mHandler.sendMessageDelayed(message, timeDelay);
    }

    protected Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == R.id.retry_task) {
                int times = msg.arg1;
                int timeDelay = msg.arg2;
                String id = (String) msg.obj;
                boolean success = onRetryTask(id);
                //不成功则重试
                if (!success) {
                    if (times > 1) {
                        Message message = mHandler.obtainMessage(R.id.retry_task, id);
                        message.arg1 = --times;
                        mHandler.sendMessageDelayed(message, timeDelay);
                    }
                }
            }
            return false;
        }
    });

    protected boolean onRetryTask(String id) {
        return false;
    }

    protected void onDestroy() {

    }
}
