package com.sankuan.jigsaw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * @Author yangkunjian.
 * @Date 2019/7/5 20:43.
 * @Desc 为什么使用getX() getY() 时不顺滑，使用getRawX() getRawY()时很顺滑，获得的信息差别是什么？RawX RawY 是相对与屏幕的位置，X,Y是什么位置呢？
 * getRawX()：表示触摸点相对于屏幕的坐标
 * getX()：表示触摸点相对于本身控件最左边和最上边的距离
 * <p>
 * 如何区分点击事件，和触摸事件？
 */

@SuppressLint("ViewConstructor")
public class MobileBlock extends View {

    private static final String TAG = "MobileBlock";

    private int unitSide;
    protected final static int DIRECTION_LEFT = 0x10;
    protected final static int DIRECTION_RIGHT = 0x11;
    protected final static int DIRECTION_DOWN = 0x12;
    protected final static int DIRECTION_UP = 0x13;
    protected final static int DIRECTION_FIX = 0x14;

    private VelocityTracker velocityTracker;

    /**
     * 初始位置/最终判定正确位置
     */
    private int mInitOrderId;

    /**
     * 当前所在位置
     */
    private int mCurrOrderId;

    /**
     * 移动后的位置
     */
    private int mMoveOrderId;

    /**
     * 是否是缺口块【true】
     */
    public boolean mIsLackBlock;

    private Paint mPaint, paint;

    private Bitmap mBitmap;

    /**
     * 触摸事件按下时的位置
     */
    private float mDownEventX, mDownEventY;
    /**
     * 触发事件移动时的实时位置
     */
    private float mMoveEventX, mMoveEventY;

    /**
     * 是否变化
     */
    private boolean forward;

    /**
     * 初始化位置，位置变化后更新
     */
    private int locationLeft,
            locationRight,
            locationTop,
            locationBottom;

    /**
     * 可移动方向
     */
    public int direction;

    private MoveFinishedListener moveFinishedListener;


    public int getInitOrderId() {
        return mInitOrderId;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setCurrOrderId(int mCurrOrderId) {
        this.mCurrOrderId = mCurrOrderId;
    }

    public int getCurrOrderId() {
        return mCurrOrderId;
    }

    public void setLocation(int locationLeft, int locationTop, int locationRight, int locationBottom) {
        this.locationLeft = locationLeft;
        this.locationTop = locationTop;
        this.locationRight = locationRight;
        this.locationBottom = locationBottom;
        Log.d(TAG, "setLocation: 更新" + mInitOrderId + "块位置");
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        Log.i(TAG, "mBitmap: width -> " + mBitmap.getWidth() + ", height -> " + mBitmap.getHeight());
    }

    public MobileBlock(Context context, int unitSide, int number, boolean isLackBlock) {
        super(context);
        setNumber(number);
        this.unitSide = unitSide;
        mIsLackBlock = isLackBlock;
        mPaint = new Paint();
        mPaint.setColor(getContext().getResources().getColor(R.color.textColor));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(12);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(12);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setMoveFinishedListener(MoveFinishedListener moveFinishedListener) {
        this.moveFinishedListener = moveFinishedListener;
    }

    private void setNumber(int mNumber) {
        this.mInitOrderId = mNumber;
        this.mCurrOrderId = mNumber;
        this.mMoveOrderId = mNumber;
    }

    /**
     * 通过触发setTop setLeft setRight setBottom 来修改块位置，触发onDraw回调
     *
     * 触发onDraw的方式：
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw: 绘制：" + mInitOrderId);
        if (mIsLackBlock) {
            // 缺口块不用绘制内容
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 5, 5, paint);
            } else {
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            }
            canvas.drawText(String.valueOf(mInitOrderId), getWidth() / 2.5f, getHeight() / 1.7f, mPaint);

            return;
        }

        // TODO: 6/21/21 画Bitmap 初始化时或选择图片后，设置的bitmap
        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap,2,2,mPaint);
//            canvas.drawBitmap(mBitmap,new Rect(locationLeft,locationTop,locationRight,locationBottom),new Rect(locationLeft,locationTop,locationRight,locationBottom),mPaint);
        }
        canvas.drawText(String.valueOf(mInitOrderId), getWidth() / 2.5f, getHeight() / 1.7f, mPaint);

        // 绘制形状色块
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            canvas.drawRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 6, 6, mPaint);
//        } else {
//            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
//        }
    }

    // TODO: 6/18/21 移动一个块时，需要联动其他的块

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 默认点击不revert
                forward = direction != DIRECTION_FIX;
                // 记录按下的位置点
                mDownEventX = event.getRawX();
                mDownEventY = event.getRawY();
                Log.i(TAG, "onTouchEvent: ACTION_DOWN -> " + mDownEventX + ", " + mDownEventY);
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);

                // 移动的时候，需要随时修改块预期的位置

                // 1. 计算手指位移量
                float moveX = 0;
                float moveY = 0;

                // 移动实时位置
                mMoveEventX = event.getRawX();
                mMoveEventY = event.getRawY();

                // 如果
                if (direction == DIRECTION_UP) {
                    // y方向上的速度 如果小于临界值，则revert 为 true; 否则看其他条件
                    float yVelocity = velocityTracker.getYVelocity();
                    Log.i(TAG, "yVelocity: " + yVelocity);

                    // 上移 范围： -UNIT_MOVE < moveY < 0
                    float fMoveY = mMoveEventY - mDownEventY;
                    if (fMoveY > 0) {
                        fMoveY = 0;
                    }
                    Log.i(TAG, "y -> " + mMoveEventY + " , fMoveY: " + fMoveY);
                    Log.i(TAG, "fMoveY: " + fMoveY);

                    moveY = -Math.min(Math.abs(fMoveY), unitSide);

                    forward = Math.abs(moveY) > (unitSide / 2.0);
                    if (!forward && yVelocity < -100) {
                        forward = true;
                    }
                } else if (direction == DIRECTION_DOWN) {
                    // y方向上的速度 如果大约临界值，则revert 为 true; 否则看其他条件
                    float yVelocity = velocityTracker.getYVelocity();
                    Log.i(TAG, "yVelocity: " + yVelocity);

                    // 下移 范围 UNIT_MOVE > moveY > 0
                    float fMoveY = mMoveEventY - mDownEventY;
                    if (fMoveY < 0) {
                        fMoveY = 0;
                    }
                    Log.i(TAG, "y -> " + mMoveEventY + " , fMoveY: " + fMoveY);
                    Log.i(TAG, "fMoveY: " + fMoveY);

                    moveY = Math.min(Math.abs(fMoveY), unitSide);

                    forward = Math.abs(moveY) > (unitSide / 2.0);
                    if (!forward && yVelocity > 100) {
                        forward = true;
                    }
                } else if (direction == DIRECTION_LEFT) {
                    // x方向上的速度 如果大约临界值，则revert 为 true; 否则看其他条件
                    float xVelocity = velocityTracker.getXVelocity();
                    Log.i(TAG, "xVelocity: " + xVelocity);

                    // 左移  范围 -UNIT_MOVE < moveX < 0
                    float fMoveX = mMoveEventX - mDownEventX;
                    Log.i(TAG, "x -> " + mMoveEventX + " , fMoveX: " + fMoveX);
                    if (fMoveX > 0) {
                        fMoveX = 0;
                    }
                    moveX = -Math.min(Math.abs(fMoveX), unitSide);

                    forward = Math.abs(moveX) > (unitSide / 2.0);
                    if (forward && xVelocity < -100) {
                        forward = true;
                    }
                } else if (direction == DIRECTION_RIGHT) {
                    // x方向上的速度 如果大约临界值，则revert 为 true; 否则看其他条件
                    float xVelocity = velocityTracker.getXVelocity();
                    Log.i(TAG, "xVelocity: " + xVelocity);

                    // 右移 范围 UNIT_MOVE > moveX > 0
                    float fMoveX = mMoveEventX - mDownEventX;
                    Log.i(TAG, "x -> " + mMoveEventX + " , fMoveX: " + fMoveX);
                    if (fMoveX < 0) {
                        fMoveX = 0;
                    }
                    moveX = Math.min(fMoveX, unitSide);

                    forward = Math.abs(moveX) > (unitSide / 2.0);
                    if (forward && xVelocity > 100) {
                        forward = true;
                    }
                }

                // 2. 计算滑块位移后的位置【位移小于等于UNIT_MOVE】  联动其他相关块，其他块随当前块运动，
                if (moveFinishedListener != null) {
                    moveFinishedListener.onMoving(moveX, moveY);
                }

                break;
            case MotionEvent.ACTION_UP:
                // 行驶到边界位置：如果超过一半 【前进】，否则【还原】
                // 如果是点击，则直接【前进】
                if (forward) {
                    move(10);
                    moveNull(direction, 10);

                    if (moveFinishedListener != null) {
                        moveFinishedListener.onMoveFinished();
                    }
                    Log.i(TAG, "forward 了");
                } else {
                    if (moveFinishedListener != null) {
                        moveFinishedListener.onMoving(0, 0);
                    }
                    Log.i(TAG, "revert 了");
                }


                break;
            default:
        }


        Log.i("事件分发:", TAG + ": onTouchEvent: return true");
        return true;
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.i("事件分发:", TAG + ": onTouchEvent: 调用");
//        boolean ss = super.onTouchEvent(event);
//        Log.i("事件分发:", TAG + ": onTouchEvent: return " + ss);
//        return ss;
//    }

    /**
     * 根据变量，控制位置
     *
     * @param moveX 横坐标移动量
     * @param moveY 纵坐标移动量
     */
    public void moveByMove(float moveX, float moveY) {
        float endTop = locationTop + moveY;
        float endBottom = locationBottom + moveY;

        float endLeft = locationLeft + moveX;
        float endRight = locationRight + moveX;

//        layout((int)endLeft,(int)endTop,(int)endRight,(int)endBottom);
        setLeft((int) endLeft);
        setTop((int) endTop);
        setRight((int) endRight);
        setBottom((int) endBottom);
    }

    /**
     * 触发移动到目标位置
     */
    public void move(int widthSize) {
        switch (direction) {
            case DIRECTION_LEFT:
                // 4-4 向左移动
                setLeft(locationLeft - unitSide);
                setRight(locationRight - unitSide);
                // 当前位置减小
                mCurrOrderId -= 1;
                break;
            case DIRECTION_RIGHT:
                // 4-3 向右移动
                setLeft(locationLeft + unitSide);
                setRight(locationRight + unitSide);
                // 当前位置增加
                mCurrOrderId += 1;
                break;
            case DIRECTION_DOWN:
                // 确定当前范围  4-1:向下移动
                setTop(locationTop + unitSide);
                setBottom(locationBottom + unitSide);
                // 当前位置增加
                mCurrOrderId += widthSize;
                break;
            case DIRECTION_UP:
                // 确定当前范围  4-2:向上移动
                setTop(locationTop - unitSide);
                setBottom(locationBottom - unitSide);
                // 当前位置减小
                mCurrOrderId -= widthSize;
                break;
            case DIRECTION_FIX:
                break;
        }

    }

    /**
     * 什么方向的【移动块】触发的【缺口】移动
     *
     * @param direction 触发【缺口】移动的块的方向
     */
    public void moveNull(int direction, int widthSize) {
        switch (direction) {
            case DIRECTION_LEFT:
                // 4-3 向右移动
                setLeft(getLeft() + unitSide);
                setRight(getRight() + unitSide);
                // 当前位置增加
                mCurrOrderId += 1;
                break;
            case DIRECTION_RIGHT:
                // 4-4 向左移动
                setLeft(getLeft() - unitSide);
                setRight(getRight() - unitSide);
                // 当前位置减小
                mCurrOrderId -= 1;
                break;
            case DIRECTION_DOWN:
                // 确定当前范围  4-2:向上移动
                setTop(getTop() - unitSide);
                setBottom(getBottom() - unitSide);
                // 当前位置减小
                mCurrOrderId -= widthSize;
                break;
            case DIRECTION_UP:
                // 确定当前范围  4-1:向下移动
                setTop(getTop() + unitSide);
                setBottom(getBottom() + unitSide);
                // 当前位置增加
                mCurrOrderId += widthSize;
                break;
            case DIRECTION_FIX:
                break;
        }
    }


    public interface MoveFinishedListener {
        /**
         * 移动完成
         */
        void onMoveFinished();

        /**
         * 移动中
         *
         * @param moveX
         * @param moveY
         */
        void onMoving(float moveX, float moveY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("事件分发", TAG + ": dispatchTouchEvent: 调用");
        boolean ss = super.dispatchTouchEvent(ev);
        Log.i("事件分发", TAG + ": dispatchTouchEvent: return " + ss);
        return ss;
    }

}
