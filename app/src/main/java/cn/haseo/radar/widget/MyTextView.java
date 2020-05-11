package cn.haseo.radar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import cn.haseo.radar.App;
import cn.haseo.radar.R;

/**
 * 自定义 TextView
 */
public class MyTextView extends AppCompatTextView {

    /**
     * 重写三个构造方法
     */
    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    /**
     * 该方法应用 xml 中所设定的字体
     */
    private void initView(Context context, AttributeSet attrs) {
        // 获取 xml 中自定义属性的集合
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTextView);
        // 从自定义属性集合中取出自定义字体的名称
        String fontName = typedArray.getString(0);

        // 设置对应的字体
        if (fontName != null) {
            setTypeface(App.getInstance().getTypeface(fontName));
        }

        // 回收自定义属性的集合
        typedArray.recycle();
    }
}
