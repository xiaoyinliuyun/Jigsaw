package com.sankuan.jigsaw;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControlZone extends ViewGroup {
    private static final String TAG = "ControlZone";

    View view;

    public ControlZone(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        removeAllViews();
        Log.i(TAG, "onLayout: l -> " + l + ", t -> " + t + ", r -> " + r + ", b -> " + b);
        view.layout(l,t,r,b);
        addView(view);
    }



    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        // 触发子view的测量
        super.measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        // 触发单个子view的测量，需要考虑当前ViewGroup【相对于待测量的子view的当前ViewGroup】的MeasureSpec和Padding
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        // 触发单个子view的测量，需要考虑当前ViewGroup的MeasureSpec和Padding,Margins
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    // getChildMeasureSpec 根据当前viewgroup的MeasureSpec信息和尺寸信息，为每个子view计算正确的MeasureSpec，通过child.measure传给子view
    // 所以：每个view的MeasureSpec都是其父view，根据自身MeasureSpec和 当前view的layoutParam确定后，传给当前view.measure的。

    private void init() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.layout_control_zone, null, true);
        view.findViewById(R.id.btn_reset).setOnClickListener(v -> {
        });
        view.findViewById(R.id.btn_auto).setOnClickListener(v -> {
        });
        view.findViewById(R.id.btn_select).setOnClickListener(v -> {
        });


//        btnReset = new Button(getContext());
//        btnReset.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        btnReset.setText("重置");
//        btnReset.setTextSize(12);
//        btnReset.layout(paddingLeft * 3 / 2 + (widthSize) * unitSide, paddingTop, paddingLeft + (widthSize + 2) * unitSide, paddingTop + unitSide);
//        btnReset.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: 2021/6/20
//            }
//        });
//        btnCompose = new Button(getContext());
//        btnCompose.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        btnCompose.setText("自动打乱");
//        btnCompose.setTextSize(12);
//        btnCompose.layout(paddingLeft * 3 / 2 + (widthSize) * unitSide, paddingTop * 4, paddingLeft + (widthSize + 2) * unitSide, paddingTop * 4 + unitSide);
//        btnCompose.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: 2021/6/20
//            }
//        });
//        btnSelectImage = new Button(getContext());
//        btnSelectImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        btnSelectImage.setText("选择图片");
//        btnSelectImage.setTextSize(12);
//        btnSelectImage.layout(paddingLeft * 3 / 2 + (widthSize) * unitSide, paddingTop * 7, paddingLeft + (widthSize + 2) * unitSide, paddingTop * 7 + unitSide);
//        btnSelectImage.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: 2021/6/20 选择图片后，用对应的比例切割图片
//            }
//        });
//
//        this.addView(btnReset);
//        this.addView(btnCompose);
//        this.addView(btnSelectImage);
    }


}
