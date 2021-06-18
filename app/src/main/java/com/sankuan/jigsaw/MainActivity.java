package com.sankuan.jigsaw;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        JigsawZone jigsawZone = findViewById(R.id.jigsaw_zone);
        if(attributes == null){
            Log.i(TAG, "attributes is null");
            return;
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(attributes.height,attributes.width);
        jigsawZone.setLayoutParams(lp);
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
