package com.sankuan.jigsaw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

@SuppressLint("ViewConstructor")
public class BackgroundZone extends View {
    private static final String TAG = "BackgroundZone";

    public int widthSize;
    public int heightSize;
    public int unitSide;

    /**
     * 内边界宽高
     */
    int width, height;

    /**
     * 内边界宽高
     */
    int innerWidth, innerHeight;
    /**
     * 内边界偏移
     */
    int left, top;

    int strokeWidth = 2;

    Path path = new Path();
    Paint paint = new Paint();

    public BackgroundZone(Context context,
                          int unitSide,
                          int widthSize,
                          int heightSize,
                          int left,
                          int top,
                          int width,
                          int height) {
        super(context);
        Log.i(TAG, "BackgroundZone: 构造");
        this.width = width;
        this.height = height;
        this.left = left;
        this.top = top;
        this.unitSide = unitSide;
        this.widthSize = widthSize;
        this.heightSize = heightSize;
        this.innerWidth = unitSide * widthSize;
        this.innerHeight = unitSide * heightSize;

        // 边框
        path.moveTo(left, top); // *
        path.lineTo(this.innerWidth + strokeWidth + left, top); // *-------
        path.lineTo(this.innerWidth + strokeWidth + left, this.innerHeight + top - unitSide); // |
        path.lineTo(this.innerWidth + strokeWidth + left + unitSide, this.innerHeight + top - unitSide); // --
        path.lineTo(this.innerWidth + strokeWidth + left + unitSide, this.innerHeight + top + strokeWidth); // |
        path.lineTo(left, this.innerHeight + top + strokeWidth); // -----*
        path.lineTo(left, top); // |
        path.close();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure: 确定测量次数");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw: BackgroundZone");


//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setAntiAlias(true);
//        paint.setStrokeWidth(60);
//        canvas.drawRect(0, 0, width, height, paint);

        // 画可移动空间线
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawPath(path, paint);

        // 画纵横线
        for (int i = 0; i < heightSize - 1; i++) {

            canvas.drawLine(
                    left,
                    top + unitSide * (i + 1),
                    left + widthSize * unitSide,
                    top + unitSide * (i + 1),
                    paint
            );
        }

        for (int i = 0; i < widthSize - 1; i++) {
            canvas.drawLine(
                    left + unitSide * (i + 1),
                    top,
                    left + +unitSide * (i + 1),
                    top + unitSide * heightSize,
                    paint
            );
        }

        // 画缺口
        canvas.drawLine(
                innerWidth + left,
                innerHeight + top - unitSide,
                innerWidth + left,
                innerHeight + top,
                paint
        );

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // view的dispatchDraw默认是空实现，而viewGroup的dispatchDraw是触发绘制子view
        super.dispatchDraw(canvas);
    }
}
