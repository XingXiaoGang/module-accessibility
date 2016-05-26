package com.gang.accessibility;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Administrator on 2016/5/6.
 */
public interface IAccessibilityProxy {

    void onAccessibilityEvent(AccessibilityEvent event);

}
