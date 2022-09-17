package com.sankuan.jigsaw;

import android.content.Context;
import android.util.TypedValue;

public class DisplayUtils {
    /**
     * dp转换成px
     *
     * @param context context对象
     * @param dpVale dp值
     * @return px值
     */
    public static int dip2px(Context context, float dpVale) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpVale * scale + 0.5f);
    }

    /**
     * sp转换成px
     *
     * @param context context对象
     * @param spValue sp值
     * @return px值
     */
    public static int dip2sp(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * px转换成dp
     *
     * @param context context对象
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
