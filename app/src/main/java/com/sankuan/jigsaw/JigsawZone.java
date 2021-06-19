package com.sankuan.jigsaw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * @Author yangkunjian.
 * @Date 2019/7/5 20:41.
 * @Dese
 */

// TODO: 6/18/21
//  (1) 触摸滑动
//  (2) 触摸抬起动画
//  (3) 选择图片加载到魔板
//  (4) 自动打乱和重置
//  (5) 其他优化和发布1.0

public class JigsawZone extends ViewGroup {
    private static final String TAG = "JigsawZone";

    public static final int WIDTH_SIZE = 12;
    public static final int HEIGHT_SIZE = 7;
    public static final int UNIT_SIDE = 50;

    /**
     * 移动块组，其中有一个是【缺口】块
     */
    private MobileBlock[] mBlocks = new MobileBlock[WIDTH_SIZE * HEIGHT_SIZE + 1];

    /**
     * 重置
     */
    private Button btnReset;

    /**
     * 自动打乱
     */
    private Button btnCompose;

    /**
     * 选择图片
     */
    private Button btnSelectImage;


    private int mLeft = 50;
    private int mTop = 50;


    public JigsawZone(Context context) {
        super(context);
        init();
    }

    public JigsawZone(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JigsawZone(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initSubViews();
    }

    /**
     * 初始化所有【移动块】
     * 1. 移动完成需要触发requestLayout()
     * 2. 点击待移动块，会导致块移动的空块，触发requestLayout()
     * 3. 点击在一行上有空的块，会导致整行移动。触发requestLayout()
     */
    private void initSubViews() {
        int changeLineTimes = 0;

        // 【移动块创建、初始化、添加到当前空间】
        for (int i = 0; i < mBlocks.length; i++) {
            // 1. 创建【移动块】对象
            final MobileBlock b = new MobileBlock(getContext(), i, i == mBlocks.length - 1);
            // 2. 添加到数组
            mBlocks[i] = b;
            // 3. 【移动块】初始化位置
            if (i % WIDTH_SIZE == 0) {
                b.layout(mLeft, mTop + UNIT_SIDE * changeLineTimes, mLeft + UNIT_SIDE, mTop + UNIT_SIDE * (changeLineTimes + 1));
                b.setLocation(mLeft, mTop + UNIT_SIDE * changeLineTimes, mLeft + UNIT_SIDE, mTop + UNIT_SIDE * (changeLineTimes + 1));
            } else {
                b.layout(mLeft + UNIT_SIDE * (i % WIDTH_SIZE), mTop + UNIT_SIDE * changeLineTimes, mLeft + UNIT_SIDE * (i % WIDTH_SIZE + 1), mTop + UNIT_SIDE * (changeLineTimes + 1));
                b.setLocation(mLeft + UNIT_SIDE * (i % WIDTH_SIZE), mTop + UNIT_SIDE * changeLineTimes, mLeft + UNIT_SIDE * (i % WIDTH_SIZE + 1), mTop + UNIT_SIDE * (changeLineTimes + 1));
                if (i % WIDTH_SIZE == WIDTH_SIZE - 1) {
                    changeLineTimes++;
                }
            }

            if (b.mIsLackBlock) {
                Log.i(TAG, "initSubViews: " + b.getInitOrderId() + ", changeLineTimes" + changeLineTimes);
                b.layout(mLeft + UNIT_SIDE * WIDTH_SIZE, mTop + UNIT_SIDE * (HEIGHT_SIZE - 1), mLeft + UNIT_SIDE * (WIDTH_SIZE + 1), mTop + UNIT_SIDE * HEIGHT_SIZE);
                b.setLocation(mLeft + UNIT_SIDE * WIDTH_SIZE, mTop + UNIT_SIDE * (HEIGHT_SIZE - 1), mLeft + UNIT_SIDE * (WIDTH_SIZE + 1), mTop + UNIT_SIDE * HEIGHT_SIZE);
            }

            // 4. 确定可移动方向
            setBlockDirection(b, WIDTH_SIZE * HEIGHT_SIZE);


//            b.setMoveFinishedListener(new MobileBlock.MoveFinishedListener() {
//                @Override
//                public void onMoveFinished(int currOrderId, int moveOrderId) {
//                    // 重新确定Block[]位置
//                    try {
//                        updateBlockArray();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
////                    // 触发UI刷新
//                    requestLayout();
//                }
//            });
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: " + b.direction);
                    // 触发联动的移动
                    moveLinkedBlocks(b);
                }
            });

            this.addView(b);
        }
        btnReset = new Button(getContext());
        btnReset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        btnReset.setText("重置");
        btnReset.setTextSize(12);
        btnReset.layout(mLeft * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, mTop, mLeft + (WIDTH_SIZE + 3) * UNIT_SIDE, mTop + UNIT_SIDE);
        btnCompose = new Button(getContext());
        btnCompose.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        btnCompose.setText("自动打乱");
        btnCompose.setTextSize(12);
        btnCompose.layout(mLeft * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, mTop * 2, mLeft + (WIDTH_SIZE + 3) * UNIT_SIDE, mTop * 2 + UNIT_SIDE);
        btnSelectImage = new Button(getContext());
        btnSelectImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        btnSelectImage.setText("选择图片");
        btnSelectImage.setTextSize(12);
        btnSelectImage.layout(mLeft * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, mTop * 3, mLeft + (WIDTH_SIZE + 3) * UNIT_SIDE, mTop * 3 + UNIT_SIDE);


        this.addView(btnReset);
        this.addView(btnCompose);
        this.addView(btnSelectImage);
    }

    public void updateDirection(int nullIndex) {
        for (MobileBlock b : mBlocks) {
            // 重新修改方向
            setBlockDirection(b, nullIndex);
        }
    }


    /**
     * 移动联动的【可移动块】
     */
    private void moveLinkedBlocks(MobileBlock b) {
        try {
            // 【缺口】位置
            int nullIndex = getNullIndex();
            // 当前【移动块】的位置
            int currOrderId = b.getCurrOrderId();
            // 联动的所有【移动块】指针
            int tempIndex = currOrderId;
            // 计算哪些块会一起联动
            switch (b.direction) {
                case MobileBlock.DIRECTION_UP:
                    while (tempIndex > nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.i(TAG, "moveUp: " + tempIndex);
                        tempIndex -= WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_DOWN:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.i(TAG, "moveDown: " + tempIndex);
                        tempIndex += WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_LEFT:
                    while (tempIndex > nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.i(TAG, "moveLeft: " + tempIndex);
                        tempIndex -= 1;
                    }
                    break;
                case MobileBlock.DIRECTION_RIGHT:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.i(TAG, "moveRight: " + tempIndex);
                        tempIndex += 1;
                    }
                    break;
                default:
                    Log.i(TAG, "direction: FIX");
            }

            // 重新确定Block[]位置
            updateBlockArray();

            // 触发UI刷新
            requestLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证成功
     */
    public boolean checkSuccess() {
        for (MobileBlock block : mBlocks) {
            if (block.getInitOrderId() != block.getCurrOrderId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 重置还原
     */
    public void reset() {

    }

    /**
     * 自动打乱
     */
    public void confuse() {

    }

    /**
     * 更新块数组 在一次移动完成后，需要及时更新数组
     * 当每个块的当前位置 等于 初始位置时 -> 成功
     *
     * @throws Exception
     */
    private void updateBlockArray() throws Exception {
        MobileBlock[] mNewBlocks = new MobileBlock[WIDTH_SIZE * HEIGHT_SIZE + 1];

        for (MobileBlock mBlock : mBlocks) {
            mNewBlocks[mBlock.getCurrOrderId()] = mBlock;
        }

        mBlocks = mNewBlocks;
        updateDirection(getNullIndex());

        if (checkSuccess()) {
            Toast.makeText(getContext(), "恭喜成功", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        System.out.println("dispatchDraw");
        int widthLen = UNIT_SIDE * WIDTH_SIZE;
        int heightLen = UNIT_SIDE * HEIGHT_SIZE;

        Path path = new Path();
        // 边框
        path.moveTo(mLeft, mTop);
        path.lineTo(widthLen + mLeft, mTop);
        path.lineTo(widthLen + mLeft, heightLen + mTop - UNIT_SIDE);
        path.lineTo(widthLen + mLeft + UNIT_SIDE, heightLen + mTop - UNIT_SIDE);
        path.lineTo(widthLen + mLeft + UNIT_SIDE, heightLen + mTop);
        path.lineTo(mLeft, heightLen + mTop);
        path.lineTo(mLeft, mTop);
        path.close();

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, widthLen + UNIT_SIDE + mLeft * 3, heightLen + mTop * 2, paint);

        // 画可移动空间线
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
        canvas.drawPath(path, paint);

        // 画纵横线
        for (int i = 0; i < HEIGHT_SIZE - 1; i++) {
            canvas.drawLine(mLeft, mTop + UNIT_SIDE * (i + 1), mLeft + WIDTH_SIZE * UNIT_SIDE, mTop + UNIT_SIDE * (i + 1), paint);
        }

        for (int i = 0; i < WIDTH_SIZE - 1; i++) {
            canvas.drawLine(mLeft + UNIT_SIDE * (i + 1), mTop, mLeft + +UNIT_SIDE * (i + 1), mTop + UNIT_SIDE * HEIGHT_SIZE, paint);
        }

        // 画缺口
        canvas.drawLine(widthLen + mLeft, heightLen + mTop - UNIT_SIDE, widthLen + mLeft, heightLen + mTop, paint);

        super.dispatchDraw(canvas);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 请求requestLayout()后，会触发此处根据【移动块】新的位置【布局】
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (!(childAt instanceof MobileBlock)) {
                continue;
            }
            MobileBlock block = (MobileBlock) getChildAt(i);
            block.layout(block.getLeft(), block.getTop(), block.getRight(), block.getBottom());
            block.setLocation(block.getLeft(), block.getTop(), block.getRight(), block.getBottom());
        }
    }

    /**
     * 根据【缺口】和 【指定块】是否在一条线上，确定【指定块】（是否可移动 和 可移动方向）
     * <p>
     * 注意：一条线上的【指定块】和【缺口】之间有【其他块】，则会一起联动。
     *
     * @param block     指定块
     * @param nullIndex 【缺口】块 index
     */
    private void setBlockDirection(MobileBlock block, int nullIndex) {
        int currOrderId = block.getCurrOrderId();
        // 根据 差异，判断当块和空间的相对位置，来确定可以往哪个方向移动
        int difference = currOrderId - nullIndex;
        if (difference == 0) {
            Log.i(TAG, "当前块 " + nullIndex + "为【缺口】块 ");
            return;
        }

        if (nullIndex == WIDTH_SIZE * HEIGHT_SIZE) {
            if (currOrderId > (nullIndex - WIDTH_SIZE - 1)) {
                // 【缺口】在最后一行的右边
                block.setDirection(MobileBlock.DIRECTION_RIGHT);
                Log.i(TAG, "block : " + currOrderId + " -> direction : 右");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.i(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (currOrderId == WIDTH_SIZE * HEIGHT_SIZE) {
            if (nullIndex >= WIDTH_SIZE * (HEIGHT_SIZE - 1)) {
                block.setDirection(MobileBlock.DIRECTION_LEFT);
                Log.i(TAG, "block : " + currOrderId + " -> direction : 左");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.i(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (nullIndex > currOrderId && (currOrderId - nullIndex) % WIDTH_SIZE == 0) {
            // 【缺口】在指定块的正下方
            block.setDirection(MobileBlock.DIRECTION_DOWN);
            Log.i(TAG, "block : " + currOrderId + " -> direction : 下");

        } else if (nullIndex < currOrderId && (currOrderId - nullIndex) % WIDTH_SIZE == 0) {
            // 【缺口】在指定块的正上方
            block.setDirection(MobileBlock.DIRECTION_UP);
            Log.i(TAG, "block : " + currOrderId + " -> direction : 上");

        } else if (nullIndex < currOrderId && currOrderId / WIDTH_SIZE == nullIndex / WIDTH_SIZE) {
            // 【缺口】在指定块的同一行的右边
            block.setDirection(MobileBlock.DIRECTION_LEFT);
            Log.i(TAG, "block : " + currOrderId + " -> direction : 左");

        } else if (nullIndex > currOrderId && currOrderId / WIDTH_SIZE == nullIndex / WIDTH_SIZE) {
            // 【缺口】在指定块的同一行的左边
            block.setDirection(MobileBlock.DIRECTION_RIGHT);
            Log.i(TAG, "block : " + currOrderId + " -> direction : 右");

        } else {
            // 【缺口】和指定块 不在一条线上
            block.setDirection(MobileBlock.DIRECTION_FIX);
            Log.i(TAG, "block : " + currOrderId + " -> direction : 固定");

        }
    }

    /**
     * 获取缺口位置index
     *
     * @return nullIndex
     */
    private int getNullIndex() throws Exception {
        for (int i = 0; i < mBlocks.length; i++) {
            if (mBlocks[i].mIsLackBlock) {
                return i;
            }
        }
        throw new Exception("未知的位置");
    }

}
