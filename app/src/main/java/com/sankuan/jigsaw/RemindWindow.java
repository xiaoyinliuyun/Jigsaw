package com.sankuan.jigsaw;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class RemindWindow {

    private WindowManager wm;
    private Context mContext;

    public RemindWindow(Context context){
        mContext = context;
    }

    public void showView(){
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);


        // Window 第一层 根View  ViewGroup
        FrameLayout myFrameLayout = new FrameLayout(mContext);
        myFrameLayout.setBackgroundColor(Color.GREEN);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();


        // 根View 宽高 小于 TextView宽高，根View限制了TextView的显示
        layoutParams.width = 100;
        layoutParams.height = 100;

        // 根View 宽高 大于 TextView宽高， 看到重叠效果
//        layoutParams.width = 500;
//        layoutParams.height = 500;

        // 根View包裹 TextView 宽度一致
//        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 根View全屏，TextView 居中显示
//        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.6f;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }else {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        }

        // Window 第二层 View
        TextView textView = new TextView(mContext);
        textView.setText("hello window");
        textView.setBackgroundColor(Color.RED);

        FrameLayout.LayoutParams textParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 400);
        textParam.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParam);

        myFrameLayout.addView(textView);

        // layoutParams 不仅约束了根view: myFrameLayout, 也约束了Window
        wm.addView(myFrameLayout, layoutParams);

        Utils.testWindowManagerGlobal();
    }
}
