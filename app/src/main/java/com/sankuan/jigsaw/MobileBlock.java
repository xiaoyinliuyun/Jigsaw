package com.sankuan.jigsaw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author yangkunjian.
 * @Date 2019/7/5 20:43.
 * @Desc 为什么使用getX() getY() 时不顺滑，使用getRawX() getRawY()时很顺滑，获得的信息差别是什么？RawX RawY 是相对与屏幕的位置，X,Y是什么位置呢？
 * getRawX()：表示触摸点相对于屏幕的坐标
 * getX()：表示触摸点相对于本身控件最左边和最上边的距离
 */

public class MobileBlock extends View {

    private static final String TAG = "MobileBlock";

    private final static int UNIT_MOVE = JigsawZone.UNIT_SIDE;
    protected final static int DIRECTION_LEFT = 0x10;
    protected final static int DIRECTION_RIGHT = 0x11;
    protected final static int DIRECTION_DOWN = 0x12;
    protected final static int DIRECTION_UP = 0x13;
    protected final static int DIRECTION_FIX = 0x14;

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

    /**
     * 触摸事件按下时的位置
     */
    private float mDownEventX, mDownEventY;
    /**
     * 触发事件移动时的实时位置
     */
    private float mMoveEventX, mMoveEventY;

    /**
     * 是否还原
     */
    private boolean revert;

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
    private int mWidthSize = JigsawZone.WIDTH_SIZE;


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
        Log.d(TAG, "setLocation: 更新"+mInitOrderId+"块位置");
    }

    public MobileBlock(Context context, int number, boolean isLackBlock) {
        super(context);
        setNumber(number);
        mIsLackBlock = isLackBlock;
        mPaint = new Paint();
        mPaint.setColor(getContext().getColor(R.color.textColor));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(12);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(12);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setMoveFinishedListener(MoveFinishedListener moveFinishedListener) {
        this.moveFinishedListener = moveFinishedListener;
    }

    public MobileBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MobileBlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setNumber(int mNumber) {
        this.mInitOrderId = mNumber;
        this.mCurrOrderId = mNumber;
        this.mMoveOrderId = mNumber;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsLackBlock) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 5, 5, paint);
//            } else {
//                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
//            }
//            canvas.drawText(String.valueOf(mInitOrderId), getWidth() / 2.5f, getHeight() / 1.7f, mPaint);

            return;
        }

//        canvas.drawText(String.valueOf(mInitOrderId), getWidth() / 2.5f, getHeight() / 1.7f, mPaint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 6, 6, mPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        }
    }

    // TODO: 6/18/21 移动一个块时，需要联动其他的块

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // 默认点击不revert
                revert = false;
                // 记录按下的位置点
                mDownEventX = event.getRawX();
                mDownEventY = event.getRawY();
                Log.d(TAG, "onTouchEvent: ACTION_DOWN -> " + mDownEventX + ", " + mDownEventY);
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动的时候，需要随时修改块预期的位置

                // 1. 计算手指位移量
                float moveX = 0;
                float moveY = 0;

                // 移动实时位置
                mMoveEventX = event.getRawX();
                mMoveEventY = event.getRawY();

                // 如果
                if(direction == DIRECTION_UP) {
                    // 上移 范围： -UNIT_MOVE < moveY < 0
                    float fMoveY = mMoveEventY - mDownEventY;
                    if(fMoveY > 0){
                        fMoveY = 0;
                    }
                    Log.d(TAG, "y -> " + mMoveEventY + " , fMoveY: " + fMoveY);
                    Log.d(TAG, "fMoveY: " + fMoveY);

                    moveY = -Math.min(Math.abs(fMoveY), UNIT_MOVE);

                    revert = Math.abs(moveY) < (UNIT_MOVE / 2.0) && Math.abs(moveY) > 5;
                } else if(direction == DIRECTION_DOWN){
                    // 下移 范围 UNIT_MOVE > moveY > 0
                    float fMoveY = mMoveEventY - mDownEventY;
                    if(fMoveY < 0){
                        fMoveY = 0;
                    }
                    Log.d(TAG, "y -> " + mMoveEventY + " , fMoveY: " + fMoveY);
                    Log.d(TAG, "fMoveY: " + fMoveY);

                    moveY = Math.min(Math.abs(fMoveY), UNIT_MOVE);

                    revert = Math.abs(moveY) < (UNIT_MOVE / 2.0) && Math.abs(moveY) > 5;
                } else if(direction == DIRECTION_LEFT){
                    // 左移  范围 -UNIT_MOVE < moveX < 0
                    float fMoveX = mMoveEventX - mDownEventX;
                    Log.d(TAG, "x -> " + mMoveEventX + " , fMoveX: " + fMoveX);
                    if(fMoveX > 0){
                        fMoveX = 0;
                    }
                    moveX = -Math.min(Math.abs(fMoveX), UNIT_MOVE);

                    revert = Math.abs(moveX) < (UNIT_MOVE / 2.0) && Math.abs(moveY) > 5;
                } else if(direction == DIRECTION_RIGHT){
                    // 右移 范围 UNIT_MOVE > moveX > 0
                    float fMoveX = mMoveEventX - mDownEventX;
                    Log.d(TAG, "x -> " + mMoveEventX + " , fMoveX: " + fMoveX);
                    if(fMoveX < 0){
                        fMoveX = 0;
                    }
                    moveX = Math.min(fMoveX, UNIT_MOVE);

                    revert = Math.abs(moveX) < (UNIT_MOVE / 2.0) && Math.abs(moveY) > 5;
                    Log.d(TAG, "moveX: " +  Math.abs(moveX));
                    Log.d(TAG, "revert: " +  revert);
                }

                // 2. 计算滑块位移后的位置【位移小于等于UNIT_MOVE】  联动其他相关块，其他块随当前块运动，
                if (moveFinishedListener != null) {
                    moveFinishedListener.onMoving(moveX, moveY);
                }

                break;
            case MotionEvent.ACTION_UP:
                // 行驶到边界位置：如果超过一半 【前进】，否则【还原】
                // 如果是点击，则直接【前进】
                if(revert) {
                    if (moveFinishedListener != null) {
                        moveFinishedListener.onMoving(0, 0);
                    }
                    Log.d(TAG, "revert 了");
                }else {
                    move();
                    moveNull(direction);

                    if (moveFinishedListener != null) {
                        moveFinishedListener.onMoveFinished(mCurrOrderId, mMoveOrderId);
                    }
                    Log.d(TAG, "forward 了");
                }


                break;
            default:
        }
        return true;
    }

    /**
     * 根据变量，控制位置
     *
     * @param moveX 横坐标移动量
     * @param moveY 纵坐标移动量
     */
    public void moveByMove(float moveX, float moveY){
        float endTop = locationTop + moveY;
        float endBottom = locationBottom + moveY;

        float endLeft = locationLeft + moveX;
        float endRight = locationRight + moveX;

//        layout((int)endLeft,(int)endTop,(int)endRight,(int)endBottom);
        setLeft((int)endLeft);
        setTop((int)endTop);
        setRight((int)endRight);
        setBottom((int)endBottom);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mEventX = event.getX();
//                mEventY = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                // 1.往哪个方向动，取决于，哪个方向为空
//                // 2.限制在这个方向上，能移动的空间
//                // 3.如何知道当前哪个方向有空位，移动后，
//
//                // 每次move 位移量
//                float moveY = event.getY() - mEventY;
//                float moveX = event.getX() - mEventX;
//
//                // 位移后的位置
//                float endTopY = getTop() + moveY;
//                float endBottomY = getBottom() + moveY;
//
//                float endLeftX = getLeft() + moveX;
//                float endRightX = getRight() + moveX;
//
//                switch (direction) {
//                    case DIRECTION_LEFT:
//                        // 4-4 向左移动
//                        moveToLeft(endLeftX, endRightX);
//                        break;
//                    case DIRECTION_RIGHT:
//                        // 4-3 向右移动
//                        moveToRight(endLeftX, endRightX);
//                        break;
//                    case DIRECTION_DOWN:
//                        // 确定当前范围  4-1:向下移动
//                        moveToDown(endTopY, endBottomY);
//                        break;
//                    case DIRECTION_UP:
//                        // 确定当前范围  4-2:向上移动
//                        moveToUp(endTopY, endBottomY);
//                        break;
//                    case DIRECTION_FIX:
//                        break;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                move();
//                // 1.通过动画移动到最终位置
//                // 2.提醒JigsawZone 移动完成，重新计算每个Block可移动方向。
//                // 3.是否需要重新布局，每一次移动，块所在的数组的位置都会变化，
////                ViewPropertyAnimator
////                if (direction == DIRECTION_RIGHT) {
////                    float moveXUp = event.getX() - mEventX;
////                    float endLeftXUp = getLeft() + moveXUp;
////                    if (endLeftXUp - locationLeft >= UNIT_MOVE / 2 && endLeftXUp - locationLeft < UNIT_MOVE) {
////                        System.out.println("前进 - > " + (UNIT_MOVE - endLeftXUp + locationLeft));
////
////                        if (mMoveOrderId == mCurrOrderId) {
////                            mMoveOrderId++;
////                        }
////                        animate().translationXBy(UNIT_MOVE - endLeftXUp + locationLeft).setDuration(200).setInterpolator(new AccelerateInterpolator()).setListener(new Animator.AnimatorListener() {
////                            @Override
////                            public void onAnimationStart(Animator animation) {
////
////                            }
////
////                            @Override
////                            public void onAnimationEnd(Animator animation) {
//////                                setLeft(locationLeft + UNIT_MOVE);
//////                                setRight(locationRight + UNIT_MOVE);
////                                if (mMoveOrderId != mCurrOrderId && moveFinishedListener != null) {
////                                    moveFinishedListener.onMoveFinished(mCurrOrderId, mMoveOrderId);
////                                }
////                            }
////
////                            @Override
////                            public void onAnimationCancel(Animator animation) {
////
////                            }
////
////                            @Override
////                            public void onAnimationRepeat(Animator animation) {
////
////                            }
////                        });
////                        return true;
////                    } else if (endLeftXUp - locationLeft < UNIT_MOVE / 2 && endLeftXUp - locationLeft > 0) {
////                        System.out.println("返回 - > " + (locationLeft - endLeftXUp));
////                        if (mMoveOrderId == mCurrOrderId + 1) {
////                            mMoveOrderId--;
////                        }
////                        System.out.println("locationLeft1 -> "+ locationLeft);
////                        System.out.println("locationRight1 -> "+ locationRight);
////                        System.out.println("translationX1 -> "+ getTranslationX());
////
////                        animate().translationXBy(locationLeft - endLeftXUp).setDuration(200).setInterpolator(new AccelerateInterpolator()).setListener(new Animator.AnimatorListener() {
////                            @Override
////                            public void onAnimationStart(Animator animation) {
////
////                            }
////
////                            @Override
////                            public void onAnimationEnd(Animator animation) {
//////                                setLeft(locationLeft);
//////                                setRight(locationRight);
//////                                setTranslationX(0);
////
////                                System.out.println("locationLeft -> "+ locationLeft);
////                                System.out.println("locationRight -> "+ locationRight);
////                                System.out.println("mMoveOrderId -> "+ mMoveOrderId);
////                                System.out.println("mCurrOrderId -> "+ mCurrOrderId);
////                                System.out.println("direction -> "+ direction);
////                                System.out.println("translationX -> "+ getTranslationX());
////
////                                if (mMoveOrderId != mCurrOrderId && moveFinishedListener != null) {
////                                    moveFinishedListener.onMoveFinished(mCurrOrderId, mMoveOrderId);
////                                }
////                            }
////
////                            @Override
////                            public void onAnimationCancel(Animator animation) {
////
////                            }
////
////                            @Override
////                            public void onAnimationRepeat(Animator animation) {
////
////                            }
////                        });
////                        return true;
////                    }
////                }
//
//                if (moveFinishedListener != null) {
//                    moveFinishedListener.onMoveFinished(mCurrOrderId, mMoveOrderId);
//                }
//                break;
//        }
//        return super.onTouchEvent(event);
//    }

//    /**
//     * 触发自动移动
//     */
//    public void move() {
//        switch (direction) {
//            case DIRECTION_LEFT:
//                // 4-4 向左移动
//                setLeft(locationLeft - UNIT_MOVE);
//                setRight(locationRight - UNIT_MOVE);
//                // 当前位置减小
//                mCurrOrderId -= 1;
//                break;
//            case DIRECTION_RIGHT:
//                // 4-3 向右移动
//                setLeft(locationLeft + UNIT_MOVE);
//                setRight(locationRight + UNIT_MOVE);
//                // 当前位置增加
//                mCurrOrderId += 1;
//                break;
//            case DIRECTION_DOWN:
//                // 确定当前范围  4-1:向下移动
//                setTop(locationTop + UNIT_MOVE);
//                setBottom(locationBottom + UNIT_MOVE);
//                // 当前位置增加
//                mCurrOrderId += JigsawZone.WIDTH_SIZE;
//                break;
//            case DIRECTION_UP:
//                // 确定当前范围  4-2:向上移动
//                setTop(locationTop - UNIT_MOVE);
//                setBottom(locationBottom - UNIT_MOVE);
//                // 当前位置减小
//                mCurrOrderId -= JigsawZone.WIDTH_SIZE;
//                break;
//            case DIRECTION_FIX:
//                break;
//        }
//
//    }

    /**
     * 触发移动到目标位置
     */
    public void move() {
        switch (direction) {
            case DIRECTION_LEFT:
                // 4-4 向左移动
                setLeft(locationLeft - UNIT_MOVE);
                setRight(locationRight - UNIT_MOVE);
                // 当前位置减小
                mCurrOrderId -= 1;
                break;
            case DIRECTION_RIGHT:
                // 4-3 向右移动
                setLeft(locationLeft + UNIT_MOVE);
                setRight(locationRight + UNIT_MOVE);
                // 当前位置增加
                mCurrOrderId += 1;
                break;
            case DIRECTION_DOWN:
                // 确定当前范围  4-1:向下移动
                setTop(locationTop + UNIT_MOVE);
                setBottom(locationBottom + UNIT_MOVE);
                // 当前位置增加
                mCurrOrderId += JigsawZone.WIDTH_SIZE;
                break;
            case DIRECTION_UP:
                // 确定当前范围  4-2:向上移动
                setTop(locationTop - UNIT_MOVE);
                setBottom(locationBottom - UNIT_MOVE);
                // 当前位置减小
                mCurrOrderId -= JigsawZone.WIDTH_SIZE;
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
    public void moveNull(int direction) {
        switch (direction) {
            case DIRECTION_LEFT:
                // 4-3 向右移动
                setLeft(getLeft() + UNIT_MOVE);
                setRight(getRight() + UNIT_MOVE);
                // 当前位置增加
                mCurrOrderId += 1;
                break;
            case DIRECTION_RIGHT:
                // 4-4 向左移动
                setLeft(getLeft() - UNIT_MOVE);
                setRight(getRight() - UNIT_MOVE);
                // 当前位置减小
                mCurrOrderId -= 1;
                break;
            case DIRECTION_DOWN:
                // 确定当前范围  4-2:向上移动
                setTop(getTop() - UNIT_MOVE);
                setBottom(getBottom() - UNIT_MOVE);
                // 当前位置减小
                mCurrOrderId -= JigsawZone.WIDTH_SIZE;
                break;
            case DIRECTION_UP:
                // 确定当前范围  4-1:向下移动
                setTop(getTop() + UNIT_MOVE);
                setBottom(getBottom() + UNIT_MOVE);
                // 当前位置增加
                mCurrOrderId += JigsawZone.WIDTH_SIZE;
                break;
            case DIRECTION_FIX:
                break;
        }
    }


    public interface MoveFinishedListener {
        /**
         * 更新移动后 【块】的当前位置
         *
         * @param currOrderId
         * @param moveOrderId
         */
        void onMoveFinished(int currOrderId, int moveOrderId);

        /**
         * 移动中
         * @param moveX
         * @param moveY
         */
        void onMoving(float moveX, float moveY);
    }
}
