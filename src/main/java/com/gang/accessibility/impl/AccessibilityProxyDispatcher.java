package com.gang.accessibility.impl;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import com.gang.accessibility.AccessibilityService;
import com.gang.accessibility.IAccessibilityProxy;

/**
 * Created by Administrator on 2016/5/6.
 */
public class AccessibilityProxyDispatcher implements IAccessibilityProxy {

    private Context mContext;
    private AccessibilityService mAccessibilityService;
    IAccessibilityProxy mProxy;

    public AccessibilityProxyDispatcher(AccessibilityService accessibilityService) {
        //不同的实现
        this.mContext = accessibilityService;
        this.mAccessibilityService = accessibilityService;
        //todo 根据不同的需求 设置不同的代理
        mProxy = new BannerAdAccessibilityImpl(accessibilityService);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (mProxy != null) {
            mProxy.onAccessibilityEvent(event);
        }
    }
}
