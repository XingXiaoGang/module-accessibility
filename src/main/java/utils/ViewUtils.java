package utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by gang on 16-5-10.
 */
public class ViewUtils {

    public static void printChilds(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            Log.d("test_ad", "viewGroup:" + group.getChildCount() + "," + group.getClass().getCanonicalName());
            for (int i = 0; i < group.getChildCount(); i++) {
                printChilds(group.getChildAt(i));
            }
        } else {
            Log.d("test_ad", "==view:" + view.getClass().getCanonicalName());
        }
    }

    public static void findWithClass(ViewGroup viewGroup, String clazz, FindCallBack callBack) {
        if (viewGroup == null || clazz == null || callBack == null) {
            return;
        }

        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = viewGroup.getChildAt(i);
            Log.d("test_ad", "====1====" + child.getClass().getCanonicalName() + ":" + clazz);
            if (child.getClass().getCanonicalName().equals(clazz)) {
                //结果
                callBack.onResult(child);
            }
            if (child instanceof ViewGroup) {
                findWithClass((ViewGroup) child, clazz, callBack);
            }
        }
    }

    public static void findWithClass(ViewGroup viewGroup, Class<? extends View> clazz, FindCallBack callBack) {
        if (viewGroup == null || clazz == null || callBack == null) {
            return;
        }

        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = viewGroup.getChildAt(i);
            Log.d("test_ad", "====2====" + child.getClass() + ":" + clazz);
            if (child.getClass().isInstance(clazz)) {
                //结果
                callBack.onResult(child);
            }
            if (child instanceof ViewGroup) {
                findWithClass((ViewGroup) child, clazz, callBack);
            }
        }
    }

    public static abstract class FindCallBack {
        public abstract void onResult(View view);
    }
}
