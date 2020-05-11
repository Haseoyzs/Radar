package cn.haseo.radar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import cn.haseo.radar.App;
import cn.haseo.radar.R;

/**
 * 自定义气泡布局
 */
public class BubbleLayout extends LinearLayout {
    // 定义圆角大小
    private int mRadius;
    // 定义三角形底边的中点
    private Point mDatumPoint;
    // 定义画笔
    private Paint mBorderPaint;
    // 定义路径
    private Path mPath;
    // 定义长方形
    private RectF mRect;


    /**
     * 重写三个构造方法
     */
    public BubbleLayout(Context context) {
        super(context);
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    /**
     * 该方法从 xml 中获取自定义的属性集合并初始化布局
     * @param context：气泡布局的上下文
     * @param attrs：xml 自定义属性集合
     */
    private void initView(Context context, AttributeSet attrs) {
        // 保证 onDraw 方法被执行
        setWillNotDraw(false);

        // 获取 xml 中自定义属性的集合
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleLayout);
        // 从自定义属性集合中获取背景颜色
        int backGroundColor = ta.getColor(R.styleable.BubbleLayout_background_color, Color.WHITE);
        // 从自定义属性集合中获取阴影颜色
        int shadowColor = ta.getColor(R.styleable.BubbleLayout_shadow_color, Color.parseColor("#999999"));
        // 从自定义属性集合中获取阴影尺寸
        int shadowSize = ta.getDimensionPixelSize(R.styleable.BubbleLayout_shadow_size, App.getInstance().dpToPx(4.0f));
        // 从自定义属性集合中获取圆角半径
        mRadius = ta.getDimensionPixelSize(R.styleable.BubbleLayout_radius, 0);
        // 回收自定义的属性集合
        ta.recycle();

        // 创建画笔
        mBorderPaint = new Paint();
        // 开启反锯齿
        mBorderPaint.setAntiAlias(true);
        // 设置画笔颜色
        mBorderPaint.setColor(backGroundColor);
        // 设置阴影
        mBorderPaint.setShadowLayer(shadowSize, 0, 0, shadowColor);

        // 创建路径
        mPath = new Path();
        // 创建长方形
        mRect = new RectF();
        // 创建三角形底边的中点
        mDatumPoint = new Point();
    }

    /**
     * 该方法根据方向画出三角形
     * @param canvas：画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底部三角形
        if (mDatumPoint.x > 0 && mDatumPoint.y > 0) {
            int triangularLength = getPaddingBottom();
            // 当布局内部偏移量为 0 时
            if (triangularLength == 0) {
                // 结束函数
                return;
            }

            //  绘制布局底部的三角形
            mPath.addRoundRect(mRect, mRadius, mRadius, Path.Direction.CCW);
            mPath.moveTo(mDatumPoint.x + (triangularLength >> 1), mDatumPoint.y);
            mPath.lineTo(mDatumPoint.x, mDatumPoint.y + (triangularLength >> 2));
            mPath.lineTo(mDatumPoint.x - (triangularLength >> 1), mDatumPoint.y);
            mPath.close();

            canvas.drawPath(mPath, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRect.left = getPaddingLeft();
        mRect.top = getPaddingTop();
        mRect.right = w - getPaddingRight();
        mRect.bottom = h - getPaddingBottom();

        mDatumPoint.x = w / 2;
        mDatumPoint.y = h - getPaddingBottom();
    }
}
