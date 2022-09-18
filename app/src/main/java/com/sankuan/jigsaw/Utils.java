package com.sankuan.jigsaw;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @Author yangkunjian.
 * @Date 5/22/21 10:26 PM.
 * @Desc
 */

class Utils {

    public static void testWindowManagerGlobal() {
        try {
            Class<?> clazz = Class.forName("android.view.WindowManagerGlobal");
            Field mViewsField = clazz.getDeclaredField("mViews");
            mViewsField.setAccessible(true);
            Field mRootsField = clazz.getDeclaredField("mRoots");
            mRootsField.setAccessible(true);
            Field mParamsField = clazz.getDeclaredField("mParams");
            mParamsField.setAccessible(true);

            Method getInstance = clazz.getMethod("getInstance");
            Object mGlobal = getInstance.invoke(null);
            Object mViews = mViewsField.get(mGlobal);
            Object mRoots = mRootsField.get(mGlobal);

            if (mViews instanceof List) {
                for (Object o : (List) mViews) {
                    printViewItem(o);
                }
            }

            if (mRoots instanceof List) {
                for (Object o : (List) mRoots) {
                    printRootItem(o);
                }
            }

        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * root 对应 ViewRootImpl
     *
     * @param viewRootImpl
     */
    private static void printRootItem(Object viewRootImpl) throws IllegalAccessException {
        Log.i("WindowTest", "viewRootImpl: " + viewRootImpl);
        Object viewObject = childCount(viewRootImpl, "mView");
        if (viewObject == null) {
            return;
        }

        Log.i("WindowTest", "遍历顶级View");
        traverseViewGroup(viewObject, 0);

        Object mContentRoot = childCount(viewObject, "mContentRoot");
        if (mContentRoot == null) {
            return;
        }


        Log.i("WindowTest", "------------------------------------------------");
    }

    private static void traverseViewGroup(Object view, int initLevel) {
        initLevel++;
        if (view instanceof ViewGroup) {
            Log.i("WindowTest", "level: " + initLevel + " , viewGroup: " + view);
            int childCount = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = ((ViewGroup) view).getChildAt(i);
                traverseViewGroup(childAt, initLevel);
            }
        } else {
            Log.i("WindowTest", "level: " + initLevel + " , view: " + view);
        }
    }

    private static Object childCount(Object parentViewGroup, String field) throws IllegalAccessException {
        Field mViewField = null;
        try {
            mViewField = parentViewGroup.getClass().getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            Log.e("WindowTest", "没有找到 field -> " + field);
            return null;
        }
        mViewField.setAccessible(true);
        Object mViewObject = mViewField.get(parentViewGroup);
        if (mViewObject == null) {
            Log.e("WindowTest", "field -> " + field + " is null");
            return null;
        }
        if (!(mViewObject instanceof ViewGroup)) {
            Log.i("WindowTest", field + " -> " + mViewObject.toString());
            return null;
        }
        int childCount = ((ViewGroup) mViewObject).getChildCount();
        StringBuilder sb = new StringBuilder();
        sb.append(field)
                .append(" -> ")
                .append(mViewObject.toString())
                .append("\n")
                .append("子视图数量 -> ")
                .append(childCount)
                .append("\n");

        for (int i = 0; i < childCount; i++) {
            View childAt = ((ViewGroup) mViewObject).getChildAt(i);
            int id = childAt.getId();
            sb.append("id -> ");
            sb.append(id);
            sb.append(" ");
            sb.append(childAt);
            sb.append("\n");
        }
        Log.i("WindowTest", sb.toString());


        return mViewObject;
    }

    /**
     * view 对应 DecorView
     *
     * @param decorView
     */
    private static void printViewItem(Object decorView) throws IllegalAccessException {
        StringBuffer sb = new StringBuffer(decorView.toString());

        Field mWindowField = null;
        try {
            mWindowField = decorView.getClass().getDeclaredField("mWindow");
            mWindowField.setAccessible(true);
            Object mWindowObject = mWindowField.get(decorView);
            sb.append("    PhoneWindow-> ").append(mWindowObject.toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            sb.append("    NO Window-> ").append(decorView.getClass().toString());
        }
        Log.i("WindowTest", "printViewItem: " + sb.toString());
    }
}
