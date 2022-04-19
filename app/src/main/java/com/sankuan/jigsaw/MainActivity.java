package com.sankuan.jigsaw;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy: ");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("onConfigurationChanged: ");
    }

    /**
     *
     * @param ev
     * @return
     *
     * 直接返回 true，false都不会向下分发，activity自己的onTouchEvent也不会执行
     *
     * 关键是调用 super.dispatchTouchEvent(ev);
     *
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("事件分发", TAG + ": dispatchTouchEvent: 调用");
        boolean ss = super.dispatchTouchEvent(ev);
        Log.i("事件分发", TAG + ": dispatchTouchEvent: return " + ss);
        return ss;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("事件分发", TAG + ": onTouchEvent: 调用");
        boolean ss = super.onTouchEvent(event);
        Log.i("事件分发", TAG + ": onTouchEvent: return " + ss);
        return ss;
    }
}
