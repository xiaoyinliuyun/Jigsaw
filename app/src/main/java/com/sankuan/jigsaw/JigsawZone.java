package com.sankuan.jigsaw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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

    /**
     * 块横向数量【应该通过配置获取】
     */
    public static final int WIDTH_SIZE = 12;
    /**
     * 块纵向数量【应该通过配置获取】
     */
    public static final int HEIGHT_SIZE = 7;
    /**
     * 单个块宽度，外部拿到的只能是整体宽高，需要根据计算，确定单元宽高
     */
    public static final int UNIT_SIDE = 120;

    /**
     * 块的起始偏移量【应该通过配置获取】
     */
    private static final int LEFT_OFFSET = 50;
    private static final int TOP_OFFSET = 50;

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



    public JigsawZone(Context context) {
        super(context);
        initSubViews();
    }

    public JigsawZone(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews();
    }

    public JigsawZone(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSubViews();
    }

    /**
     * 初始化所有【移动块】
     * 1. 移动完成需要触发requestLayout()
     * 2. 点击待移动块，会导致块移动的空块，触发requestLayout()
     * 3. 点击在一行上有空的块，会导致整行移动。触发requestLayout()
     */
    private void initSubViews() {
        // 先有设置或测量得到的宽度



        // 添加背景view
        BackgroundZone zone = new BackgroundZone(getContext(), LEFT_OFFSET, TOP_OFFSET, WIDTH_SIZE*UNIT_SIDE, HEIGHT_SIZE * UNIT_SIDE);
        zone.layout(0, 0, (WIDTH_SIZE + 3) * UNIT_SIDE ,   (HEIGHT_SIZE + 2) * UNIT_SIDE);
        this.addView(zone);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_car_1);
        Log.i(TAG, "bitmap: width -> " + bitmap.getWidth() + ", height -> " + bitmap.getHeight());


        int changeLineTimes = 0;

        // 【移动块创建、初始化、添加到当前空间】
        for (int i = 0; i < mBlocks.length; i++) {
            // 1. 创建【移动块】对象
            final MobileBlock b = new MobileBlock(getContext(), i, i == mBlocks.length - 1);
            // 2. 添加到数组
            mBlocks[i] = b;
            // 3. 【移动块】初始化位置
            if (i % WIDTH_SIZE == 0) {
                b.layout(LEFT_OFFSET, TOP_OFFSET + UNIT_SIDE * changeLineTimes, LEFT_OFFSET + UNIT_SIDE, TOP_OFFSET + UNIT_SIDE * (changeLineTimes + 1));
                b.setLocation(LEFT_OFFSET, TOP_OFFSET + UNIT_SIDE * changeLineTimes, LEFT_OFFSET + UNIT_SIDE, TOP_OFFSET + UNIT_SIDE * (changeLineTimes + 1));
            } else {
                b.layout(LEFT_OFFSET + UNIT_SIDE * (i % WIDTH_SIZE), TOP_OFFSET + UNIT_SIDE * changeLineTimes, LEFT_OFFSET + UNIT_SIDE * (i % WIDTH_SIZE + 1), TOP_OFFSET + UNIT_SIDE * (changeLineTimes + 1));
                b.setLocation(LEFT_OFFSET + UNIT_SIDE * (i % WIDTH_SIZE), TOP_OFFSET + UNIT_SIDE * changeLineTimes, LEFT_OFFSET + UNIT_SIDE * (i % WIDTH_SIZE + 1), TOP_OFFSET + UNIT_SIDE * (changeLineTimes + 1));
                if (i % WIDTH_SIZE == WIDTH_SIZE - 1) {
                    changeLineTimes++;
                }
            }

            if (b.mIsLackBlock) {
                Log.i(TAG, "initSubViews: " + b.getInitOrderId() + ", changeLineTimes" + changeLineTimes);
                b.layout(LEFT_OFFSET + UNIT_SIDE * WIDTH_SIZE, TOP_OFFSET + UNIT_SIDE * (HEIGHT_SIZE - 1), LEFT_OFFSET + UNIT_SIDE * (WIDTH_SIZE + 1), TOP_OFFSET + UNIT_SIDE * HEIGHT_SIZE);
                b.setLocation(LEFT_OFFSET + UNIT_SIDE * WIDTH_SIZE, TOP_OFFSET + UNIT_SIDE * (HEIGHT_SIZE - 1), LEFT_OFFSET + UNIT_SIDE * (WIDTH_SIZE + 1), TOP_OFFSET + UNIT_SIDE * HEIGHT_SIZE);
            }
            // 设置图片
            if(b.getLeft() + UNIT_SIDE <= bitmap.getWidth() && b.getTop() + UNIT_SIDE <= bitmap.getHeight()) {
                b.setBitmap(Bitmap.createBitmap(bitmap, b.getLeft(), b.getTop(), UNIT_SIDE, UNIT_SIDE));
            }
            // 4. 确定可移动方向
            setBlockDirection(b, WIDTH_SIZE * HEIGHT_SIZE);


            b.setMoveFinishedListener(new MobileBlock.MoveFinishedListener() {
                @Override
                public void onMoving(float moveX, float moveY) {
                    movingLinkedBlocks(b, moveX, moveY);
                }

                @Override
                public void onMoveFinished() {
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
        btnReset.layout(LEFT_OFFSET * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, TOP_OFFSET, LEFT_OFFSET + (WIDTH_SIZE + 3) * UNIT_SIDE, TOP_OFFSET + UNIT_SIDE);
        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2021/6/20
            }
        });
        btnCompose = new Button(getContext());
        btnCompose.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        btnCompose.setText("自动打乱");
        btnCompose.setTextSize(12);
        btnCompose.layout(LEFT_OFFSET * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, TOP_OFFSET * 4, LEFT_OFFSET + (WIDTH_SIZE + 3) * UNIT_SIDE, TOP_OFFSET * 4 + UNIT_SIDE);
        btnCompose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2021/6/20
            }
        });
        btnSelectImage = new Button(getContext());
        btnSelectImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        btnSelectImage.setText("选择图片");
        btnSelectImage.setTextSize(12);
        btnSelectImage.layout(LEFT_OFFSET * 3 / 2 + (WIDTH_SIZE) * UNIT_SIDE, TOP_OFFSET * 7, LEFT_OFFSET + (WIDTH_SIZE + 3) * UNIT_SIDE, TOP_OFFSET * 7 + UNIT_SIDE);
        btnSelectImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2021/6/20 选择图片后，用对应的比例切割图片
            }
        });

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

    private void movingLinkedBlocks(MobileBlock b, float moveX, float moveY) {
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
                        mBlocks[tempIndex].moveByMove(moveX, moveY);
                        Log.d(TAG, "moveUp: " + tempIndex);
                        tempIndex -= WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_DOWN:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].moveByMove(moveX, moveY);
                        Log.d(TAG, "moveDown: " + tempIndex);
                        tempIndex += WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_LEFT:
                    while (tempIndex > nullIndex) {
                        mBlocks[tempIndex].moveByMove(moveX, moveY);
                        Log.d(TAG, "moveLeft: " + tempIndex);
                        tempIndex -= 1;
                    }
                    break;
                case MobileBlock.DIRECTION_RIGHT:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].moveByMove(moveX, moveY);
                        Log.d(TAG, "moveRight: " + tempIndex);
                        tempIndex += 1;
                    }
                    break;
                default:
                    Log.d(TAG, "direction: FIX");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                        Log.d(TAG, "moveUp: " + tempIndex);
                        tempIndex -= WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_DOWN:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.d(TAG, "moveDown: " + tempIndex);
                        tempIndex += WIDTH_SIZE;
                    }
                    break;
                case MobileBlock.DIRECTION_LEFT:
                    while (tempIndex > nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.d(TAG, "moveLeft: " + tempIndex);
                        tempIndex -= 1;
                    }
                    break;
                case MobileBlock.DIRECTION_RIGHT:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move();
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction);
                        Log.d(TAG, "moveRight: " + tempIndex);
                        tempIndex += 1;
                    }
                    break;
                default:
                    Log.d(TAG, "direction: FIX");
            }

            // 重新确定Block[]位置
            updateBlockArray();

            Log.i(TAG, "moveLinkedBlocks: 触发UI刷新");
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



    /**
     * 1. 绘制区域背景
     * 2. 绘制可移动范围
     *
     * 这个会调用多次，不应该在此绘制背景
     *
     * @param canvas
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.i(TAG, "dispatchDraw: ");
        
        super.dispatchDraw(canvas);
    }


    /**
     * 如果触摸事件结束后，布局有变化时，调用requestLayout()，触发重新绘制
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "JigsawZone: onLayout 重新布局");
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
            Log.d(TAG, "当前块 " + nullIndex + "为【缺口】块 ");
            return;
        }

        if (nullIndex == WIDTH_SIZE * HEIGHT_SIZE) {
            if (currOrderId > (nullIndex - WIDTH_SIZE - 1)) {
                // 【缺口】在最后一行的右边
                block.setDirection(MobileBlock.DIRECTION_RIGHT);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 右");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (currOrderId == WIDTH_SIZE * HEIGHT_SIZE) {
            if (nullIndex >= WIDTH_SIZE * (HEIGHT_SIZE - 1)) {
                block.setDirection(MobileBlock.DIRECTION_LEFT);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 左");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (nullIndex > currOrderId && (currOrderId - nullIndex) % WIDTH_SIZE == 0) {
            // 【缺口】在指定块的正下方
            block.setDirection(MobileBlock.DIRECTION_DOWN);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 下");

        } else if (nullIndex < currOrderId && (currOrderId - nullIndex) % WIDTH_SIZE == 0) {
            // 【缺口】在指定块的正上方
            block.setDirection(MobileBlock.DIRECTION_UP);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 上");

        } else if (nullIndex < currOrderId && currOrderId / WIDTH_SIZE == nullIndex / WIDTH_SIZE) {
            // 【缺口】在指定块的同一行的右边
            block.setDirection(MobileBlock.DIRECTION_LEFT);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 左");

        } else if (nullIndex > currOrderId && currOrderId / WIDTH_SIZE == nullIndex / WIDTH_SIZE) {
            // 【缺口】在指定块的同一行的左边
            block.setDirection(MobileBlock.DIRECTION_RIGHT);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 右");

        } else {
            // 【缺口】和指定块 不在一条线上
            block.setDirection(MobileBlock.DIRECTION_FIX);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 固定");

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("事件分发", TAG+": dispatchTouchEvent: 调用");
        boolean ss = super.dispatchTouchEvent(ev);
        Log.i("事件分发", TAG+": dispatchTouchEvent: return " + ss);
        return ss;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i("事件分发", TAG + ": onInterceptTouchEvent: 调用");
        boolean ss = super.onInterceptTouchEvent(ev);
        Log.i("事件分发", TAG + ": onInterceptTouchEvent: return " + ss);
        return ss;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("事件分发", TAG + ": onTouchEvent: 调用");
        boolean ss = super.onTouchEvent(event);
        Log.i("事件分发", TAG + ": onTouchEvent: return " + ss);
        return ss;
    }
}
