package com.gang.accessibility;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 用于突破api18以上无法隐藏常驻通知的限制（原理：启动两个Service绑定通知栏，然后关掉一个service，第一个service就能一直运行了 0.0）
 * Created by jiangyinbin on 2016/2/14.
 */
public class InnerService extends Service {

    public static final String ACTION_STOP = "stop";

    private static final boolean DEBUG = false;
    private static final String TAG = DEBUG ? "InnerService" : "I";

    public static final int NOTIFICATION_ID = 1017;

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            Log.i(TAG, "InnerService onCreate");
        }
        try {
            // 先把自己也搞成前台的，提供合法参数
            startForeground(NOTIFICATION_ID, fadeNotification(this));
            // 关键步骤来了：自行推掉，或者把AlipayService退掉。
            // duang！系统sb了，说好的人与人的信任呢？
            stopSelf();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "err", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (DEBUG) {
            Log.i(TAG, "InnerService onDestroy");
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_STOP: {
                        stopForeground(true);
                        stopSelf();
                        break;
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 随便显示一个正常的Service，反正不会显示，只是假装自己是合法的Notification而已
     */
    private static Notification fadeNotification(Context context) {
        Notification notification = new Notification();
        notification.icon = R.mipmap.ic_launcher;
        notification.contentView = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);
        return notification;
    }

    /**
     * 显示隐藏的常驻通知栏，用于常驻Service
     *
     * @param service 将被常驻的Service
     */
    public static void showHideNotification(Service service) {
        if (Build.VERSION.SDK_INT < 18) {
            // 4.3以下系统会隐藏这个空通知栏
            Notification notification = new Notification();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            if (Build.VERSION.SDK_INT > 15) {
                notification.priority = Notification.PRIORITY_MAX;
            }
            service.startForeground(NOTIFICATION_ID, notification);
        } else {
            // api 18的时候，google管严了，得绕着玩
            // 先把自己做成一个前台服务，提供合法的参数
            service.startForeground(NOTIFICATION_ID, fadeNotification(service.getBaseContext()));
            // 再起一个服务，也是前台的
            service.startService(new Intent(service.getBaseContext(), InnerService.class));
        }
    }

    public static void stopDemon(Service service) {
        Intent intent = new Intent(service.getBaseContext(), InnerService.class);
        intent.setAction(InnerService.ACTION_STOP);
        service.startService(new Intent(service.getBaseContext(), InnerService.class));
    }

}
