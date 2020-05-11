package cn.haseo.radar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import cn.haseo.radar.App;
import cn.haseo.radar.R;


/**
 * 自定义圆形布局
 * 通过自定义属性来控制圆角的半径，当正方形圆角半径等于边长的二分之一时，此时布局为一个圆形
 */
public class CircleLayout extends FrameLayout {
    // 定义一个路径
    private Path path;
    // 定义一个矩形
    private RectF rectF;
    // 定义圆角半径
    private float cornerRadius;


    /**
     * 重写三个构造方法
     */
    public CircleLayout(Context context) {
        super(context);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    /**
     * 该方法用于初始化布局
     * @param context：圆形布局的上下文
     * @param attrs：xml 属性集合
     */
    private void initView(Context context,AttributeSet attrs) {
        // 保证 onDraw 方法被执行
        setWillNotDraw(false);

        // 创建路径
        path = new Path();
        // 创建矩形
        rectF = new RectF();

        // 获取 xml 中自定义属性的集合
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout);
        // 从自定义属性集合中获取圆角半径
        cornerRadius = typedArray.getDimension(R.styleable.CircleLayout_corner_radius, 0);
        // 回收自定义的属性集合
        typedArray.recycle();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 定义一个左上角位于布局坐标系原点，长和宽等于布局长宽的矩形
        rectF.set(0.0f, 0.0f, getMeasuredWidth(), getMeasuredHeight());
        // 添加一个圆角矩形路径到 path 中，圆角的长半轴和短半轴都为 cornerRadius，Path.Direction.CW 表示顺时针画出路径
        path.addRoundRect(rectF, App.getInstance().dpToPx(cornerRadius), App.getInstance().dpToPx(cornerRadius), Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        // 当圆角半径大于 0 时
        if (cornerRadius > 0.0f) {
            // 按照路径裁剪画布
            canvas.clipPath(path);
        }
        super.draw(canvas);
    }
}