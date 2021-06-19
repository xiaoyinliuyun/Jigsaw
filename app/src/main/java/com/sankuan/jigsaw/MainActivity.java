package com.sankuan.jigsaw;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
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
}
