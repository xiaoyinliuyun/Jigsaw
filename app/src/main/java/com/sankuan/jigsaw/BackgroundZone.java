package com.sankuan.jigsaw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

public class BackgroundZone extends View {
    private static final String TAG = "BackgroundZone";

    public static final int WIDTH_SIZE = 12;
    public static final int HEIGHT_SIZE = 7;
    public static final int UNIT_SIDE = 120;

    /**
     * 内边界宽高
     */
    int width, height;
    /**
     * 内边界偏移
     */
    int left, top;

    Path path = new Path();
    Paint paint = new Paint();

    public BackgroundZone(Context context, int left, int top, int width, int height) {
        super(context);
        Log.i(TAG, "BackgroundZone: 构造");
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        // 边框
        path.moveTo(left, top);
        path.lineTo(width + left, top);
        path.lineTo(width + left, this.height + top - UNIT_SIDE);
        path.lineTo(width + left + UNIT_SIDE, this.height + top - UNIT_SIDE);
        path.lineTo(width + left + UNIT_SIDE, this.height + top);
        path.lineTo(left, this.height + top);
        path.lineTo(left, top);
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw: BackgroundZone");


        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width + UNIT_SIDE + left * 3, height + top * 2, paint);

        // 画可移动空间线
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
        canvas.drawPath(path, paint);

        // 画纵横线
        for (int i = 0; i < HEIGHT_SIZE - 1; i++) {
            canvas.drawLine(left, top + UNIT_SIDE * (i + 1), left + WIDTH_SIZE * UNIT_SIDE, top + UNIT_SIDE * (i + 1), paint);
        }

        for (int i = 0; i < WIDTH_SIZE - 1; i++) {
            canvas.drawLine(left + UNIT_SIDE * (i + 1), top, left + +UNIT_SIDE * (i + 1), top + UNIT_SIDE * HEIGHT_SIZE, paint);
        }

        // 画缺口
        canvas.drawLine(width + left, height + top - UNIT_SIDE, width + left, height + top, paint);

    }
}
