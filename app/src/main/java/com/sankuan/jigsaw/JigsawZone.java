package com.sankuan.jigsaw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    public static final int WIDTH_SIZE = 12; // WIDTH_SIZE
    /**
     * 块纵向数量【应该通过配置获取】
     */
    public static final int HEIGHT_SIZE = 7; // HEIGHT_SIZE
    /**
     * 单个块宽度，外部拿到的只能是整体宽高，需要根据计算，确定单元宽高
     */
    public static final int DEFAULT_UNIT_SIDE = 120;

    /**
     * 实际单元宽度
     */
    public int unitSide = DEFAULT_UNIT_SIDE;

    public int resId;

    public int width;
    public int height;

    public int widthSize;
    public int heightSize;

    /**
     * 移动块组，其中有一个是【缺口】块
     */
    private MobileBlock[] mBlocks;

    Bitmap bitmap;

    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;

    ViewGroup controlZone;


    public JigsawZone(Context context) {
        this(context, null);
    }

    public JigsawZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JigsawZone(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSubViews(context, attrs, defStyleAttr);
    }

    /**
     * 初始化所有【移动块】
     * 1. 移动完成需要触发requestLayout()
     * 2. 点击待移动块，会导致块移动的空块，触发requestLayout()
     * 3. 点击在一行上有空的块，会导致整行移动。触发requestLayout()
     */
    private void initSubViews(Context context, AttributeSet attrs, int defStyleAttr) {
        // 先有设置或测量得到的宽度


        // 可以调整的应该是块的大小，从配置里取值块大小

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JigsawZone);
        unitSide = a.getDimensionPixelSize(R.styleable.JigsawZone_blockSize, DEFAULT_UNIT_SIDE);
        resId = a.getResourceId(R.styleable.JigsawZone_src, R.mipmap.ic_cat);
        a.recycle();

        bitmap = BitmapFactory.decodeResource(getResources(), resId);

        Log.i(TAG, "bitmap: width -> " + bitmap.getWidth() + ", height -> " + bitmap.getHeight());

        // 如何转换 把整个图片按块分割

    }

    /**
     * 根据模式 确认Size
     *
     * @param widthMeasureSpec
     * @param width
     * @return
     */
    private int measureWidth(int widthMeasureSpec, int width) {
        int result; // 模式

        // widthMeasureSpec: 00000000000000000000000000000000
        // and MODE_MASK   : 11000000000000000000000000000000
        // and ~MODE_MASK  : 00111111111111111111111111111111

        // MeasureSpec 封装了从父级传递到子级的布局要求。
        // 每个 MeasureSpec 代表对宽度或高度的要求。
        // MeasureSpec 由大小和模式组成。

        // UNSPECIFIED : 父级没有对当前View施加任何限制。它可以是任何它想要的大小。
        // EXACTLY: 父级已经确定了当前View的确切尺寸。无论孩子想要多大，都将获得这些界限。
        // AT_MOST: 孩子可以根据需要达到指定的大小
        int specMode = MeasureSpec.getMode(widthMeasureSpec); // 前两位的值
        int specSize = MeasureSpec.getSize(widthMeasureSpec); // 后30为的值
        switch (specMode) {
            // 父级 的模式 还是 当前View的模式
            case MeasureSpec.UNSPECIFIED: // 00000000000000000000000000000000
                result = width;
                break;
            case MeasureSpec.EXACTLY:     // 01000000000000000000000000000000
            case MeasureSpec.AT_MOST:     // 10000000000000000000000000000000
                result = specSize;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + specMode);
        }
        return result;
    }

    /**
     * 根据 模式，确认Size
     *
     * @param heightMeasureSpec
     * @param height
     * @return
     */
    private int measureHeight(int heightMeasureSpec, int height) {
        return 0;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure+: Mode -> " + MeasureSpec.getMode(widthMeasureSpec) + ", Size -> " + MeasureSpec.getSize(widthMeasureSpec));
        Log.i(TAG, "onMeasure+: Mode -> " + MeasureSpec.getMode(heightMeasureSpec) + ", Size -> " + MeasureSpec.getSize(heightMeasureSpec));

        // -2147483648  10 AT_MOST
        // 1073741824 01  EXACTLY

        if (mBlocks == null) {
            paddingLeft = getPaddingLeft();
            paddingTop = getPaddingTop();
            paddingRight = getPaddingRight();
            paddingBottom = getPaddingBottom();

            Log.i(TAG, "onMeasure: paddingLeft -> " + paddingLeft);
            Log.i(TAG, "onMeasure: paddingTop -> " + paddingTop);
            Log.i(TAG, "onMeasure: paddingRight -> " + paddingRight);
            Log.i(TAG, "onMeasure: paddingBottom -> " + paddingBottom);

            // 可用通过下面的方式 获取控件宽高，但不一定通过getWidth() 或 getHeight() 获取宽高
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            Log.i(TAG, "onMeasure: width -> " + width + ", width ->" + height);

            // 根据宽高，计算出横向上的数量 和 纵向上的数量，注意偏移
            widthSize = (width - paddingLeft - paddingRight) / unitSide - 4;
            heightSize = (height - paddingTop - paddingBottom) / unitSide;
            Log.i(TAG, "onMeasure: widthSize -> " + widthSize + ", heightSize ->" + heightSize);


            // 添加背景view
            BackgroundZone zone = new BackgroundZone(getContext(),
                    unitSide, // 块边长
                    widthSize, heightSize, // 块数量
                    paddingLeft, paddingTop, // 内边界偏移
                    width, // 整体宽
                    height // 整体高
            );
            zone.layout(0, 0, (widthSize + 3) * unitSide, (heightSize + 2) * unitSide);
            addView(zone);

            mBlocks = new MobileBlock[widthSize * heightSize + 1];
            int changeLineTimes = 0;
            // 【移动块创建、初始化、添加到当前空间】
            for (int i = 0; i < mBlocks.length; i++) {
                // 1. 创建【移动块】对象
                final MobileBlock b = new MobileBlock(getContext(), unitSide, i, i == mBlocks.length - 1);
                // 2. 添加到数组
                mBlocks[i] = b;
                // TODO: 2022/9/17 优化点
                b.setWidthSize(widthSize);
                // 3. 【移动块】初始化位置
                if (i % widthSize == 0) {
                    b.layout(paddingLeft, paddingTop + unitSide * changeLineTimes, paddingLeft + unitSide, paddingTop + unitSide * (changeLineTimes + 1));
                    b.setLocation(paddingLeft, paddingTop + unitSide * changeLineTimes, paddingLeft + unitSide, paddingTop + unitSide * (changeLineTimes + 1));
                } else {
                    b.layout(paddingLeft + unitSide * (i % widthSize), paddingTop + unitSide * changeLineTimes, paddingLeft + unitSide * (i % widthSize + 1), paddingTop + unitSide * (changeLineTimes + 1));
                    b.setLocation(paddingLeft + unitSide * (i % widthSize), paddingTop + unitSide * changeLineTimes, paddingLeft + unitSide * (i % widthSize + 1), paddingTop + unitSide * (changeLineTimes + 1));
                    if (i % widthSize == widthSize - 1) {
                        changeLineTimes++;
                    }
                }

                if (b.mIsLackBlock) {
                    Log.i(TAG, "initSubViews: " + b.getInitOrderId() + ", changeLineTimes" + changeLineTimes);
                    b.layout(paddingLeft + unitSide * widthSize, paddingTop + unitSide * (heightSize - 1), paddingLeft + unitSide * (widthSize + 1), paddingTop + unitSide * heightSize);
                    b.setLocation(paddingLeft + unitSide * widthSize, paddingTop + unitSide * (heightSize - 1), paddingLeft + unitSide * (widthSize + 1), paddingTop + unitSide * heightSize);
                }
                // 设置图片
                if (b.getLeft() + unitSide <= bitmap.getWidth() && b.getTop() + unitSide <= bitmap.getHeight()) {
                    b.setBitmap(Bitmap.createBitmap(bitmap, b.getLeft(), b.getTop(), unitSide, unitSide));
                }
                // 4. 确定可移动方向
                setBlockDirection(b, widthSize * heightSize);


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
                addView(b);
            }


            controlZone = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_control_zone, null, false);

            Log.i(TAG, "Button: 子组件数量 -> " + controlZone.getChildCount());
            Log.i(TAG, "Button controlZone: width -> " + controlZone.getWidth() + ", height -> " + controlZone.getHeight());
            Log.i(TAG, "Button controlZone: left -> " + controlZone.getLeft() + ", top -> " + controlZone.getTop() + ", right -> " + controlZone.getRight() + ", bottom -> " + controlZone.getBottom());

            controlZone.findViewById(R.id.btn_reset).setOnClickListener(v -> {
                Toast.makeText(getContext(), "重置", Toast.LENGTH_SHORT).show();
            });
            controlZone.findViewById(R.id.btn_auto).setOnClickListener(v -> {
                Toast.makeText(getContext(), "自动打乱", Toast.LENGTH_SHORT).show();
            });
            controlZone.findViewById(R.id.btn_select).setOnClickListener(v -> {
                Toast.makeText(getContext(), "选择图片", Toast.LENGTH_SHORT).show();
            });
            controlZone.layout(paddingLeft * 3 / 2 + (widthSize + 1) * unitSide, paddingTop * 7, paddingLeft + (widthSize + 4) * unitSide, paddingTop * 8 + unitSide * 3);

            for (int i = 0; i < controlZone.getChildCount(); i++) {
                View childAt = controlZone.getChildAt(i);
                if (childAt instanceof Button) {
                    Log.i(TAG, "Button " + i + " childAt: width -> " + childAt.getWidth() + ", height -> " + childAt.getHeight());
                    Log.i(TAG, "Button " + i + " childAt: left -> " + childAt.getLeft() + ", top -> " + childAt.getTop() + ", right -> " + childAt.getRight() + ", bottom -> " + childAt.getBottom());
                    // todo 注意：子view的位置是相对于其父view，而不是当前view。
                    childAt.layout(0, i * unitSide / 5 * 4 + unitSide, unitSide * 2, (i + 1) * unitSide / 5 * 4 + unitSide);
                    ((Button) childAt).setGravity(Gravity.CENTER);
                }
            }
            addView(controlZone);

            for (int i = 0; i < this.getChildCount(); i++) {
                View child = this.getChildAt(i);
                if (child instanceof LinearLayout) {
                    for (int j = 0; j < ((LinearLayout) child).getChildCount(); j++) {
                        View childAt = ((LinearLayout) child).getChildAt(j);
                        if (childAt instanceof Button) {
                            Log.i(TAG, "Button " + j + " childAt: width -> " + childAt.getWidth() + ", height -> " + childAt.getHeight());
                            Log.i(TAG, "Button " + j + " childAt: left -> " + childAt.getLeft() + ", top -> " + childAt.getTop() + ", right -> " + childAt.getRight() + ", bottom -> " + childAt.getBottom());
                            Log.i(TAG, "Button " + j + " childAt: text -> " + ((Button) childAt).getText() + ", size -> " + ((Button) childAt).getTextSize() + ", alpha -> " + childAt.getAlpha() + ", color -> " + ((Button) childAt).getTextColors());
                        }
                    }
                }
            }

        }
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
                        tempIndex -= widthSize;
                    }
                    break;
                case MobileBlock.DIRECTION_DOWN:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].moveByMove(moveX, moveY);
                        Log.d(TAG, "moveDown: " + tempIndex);
                        tempIndex += widthSize;
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
                        mBlocks[tempIndex].move(widthSize);
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction, widthSize);
                        Log.d(TAG, "moveUp: " + tempIndex);
                        tempIndex -= widthSize;
                    }
                    break;
                case MobileBlock.DIRECTION_DOWN:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move(widthSize);
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction, widthSize);
                        Log.d(TAG, "moveDown: " + tempIndex);
                        tempIndex += widthSize;
                    }
                    break;
                case MobileBlock.DIRECTION_LEFT:
                    while (tempIndex > nullIndex) {
                        mBlocks[tempIndex].move(widthSize);
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction, widthSize);
                        Log.d(TAG, "moveLeft: " + tempIndex);
                        tempIndex -= 1;
                    }
                    break;
                case MobileBlock.DIRECTION_RIGHT:
                    while (tempIndex < nullIndex) {
                        mBlocks[tempIndex].move(widthSize);
                        mBlocks[nullIndex].moveNull(mBlocks[tempIndex].direction, widthSize);
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
     * 更新块数组 在一次移动完成后，需要及时更新数组
     * 当每个块的当前位置 等于 初始位置时 -> 成功
     *
     * @throws Exception
     */
    private void updateBlockArray() throws Exception {
        MobileBlock[] mNewBlocks = new MobileBlock[widthSize * heightSize + 1];

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
     * <p>
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
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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

        if (nullIndex == widthSize * heightSize) {
            if (currOrderId > (nullIndex - widthSize - 1)) {
                // 【缺口】在最后一行的右边
                block.setDirection(MobileBlock.DIRECTION_RIGHT);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 右");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (currOrderId == widthSize * heightSize) {
            if (nullIndex >= widthSize * (heightSize - 1)) {
                block.setDirection(MobileBlock.DIRECTION_LEFT);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 左");
            } else {
                // 【缺口】和指定块 不在一条线上
                block.setDirection(MobileBlock.DIRECTION_FIX);
                Log.d(TAG, "block : " + currOrderId + " -> direction : 固定");
            }
        } else if (nullIndex > currOrderId && (currOrderId - nullIndex) % widthSize == 0) {
            // 【缺口】在指定块的正下方
            block.setDirection(MobileBlock.DIRECTION_DOWN);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 下");

        } else if (nullIndex < currOrderId && (currOrderId - nullIndex) % widthSize == 0) {
            // 【缺口】在指定块的正上方
            block.setDirection(MobileBlock.DIRECTION_UP);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 上");

        } else if (nullIndex < currOrderId && currOrderId / widthSize == nullIndex / widthSize) {
            // 【缺口】在指定块的同一行的右边
            block.setDirection(MobileBlock.DIRECTION_LEFT);
            Log.d(TAG, "block : " + currOrderId + " -> direction : 左");

        } else if (nullIndex > currOrderId && currOrderId / widthSize == nullIndex / widthSize) {
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
        Log.i("事件分发", TAG + ": dispatchTouchEvent: 调用");
        boolean ss = super.dispatchTouchEvent(ev);
        Log.i("事件分发", TAG + ": dispatchTouchEvent: return " + ss);
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
