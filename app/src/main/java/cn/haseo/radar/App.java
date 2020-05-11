package cn.haseo.radar;

import android.annotation.SuppressLint;
import android.graphics.Typeface;

import com.baidu.mapapi.SDKInitializer;

import org.litepal.LitePalApplication;

/**
 * 自定义 Application 类
 */
@SuppressLint("StaticFieldLeak")
public class App extends LitePalApplication {
    // 定义 Application 实例
    private static App app;
    // 用于标记数据是否改变
    private boolean dataChange = false;

    // 定义各字体
    private Typeface _7thc;
    private Typeface _7thi;
    private Typeface _04b24;
    private Typeface _04b03b;

    public static App getInstance() {
        return app;
    }
    public boolean isDataChange() {
        return dataChange;
    }
    public void setDataChange(boolean dataChange) {
        this.dataChange = dataChange;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 获取 App 实例
        app = this;

        // 初始化 SDK
        SDKInitializer.initialize(this);

        // 初始化各字体
        _7thc = Typeface.createFromAsset(getAssets(), "fonts/7thServiceCondensed.ttf");
        _7thi = Typeface.createFromAsset(getAssets(), "fonts/7thServiceItalic.ttf");
        _04b24 = Typeface.createFromAsset(getAssets(), "fonts/04b24.ttf");
        _04b03b = Typeface.createFromAsset(getAssets(), "fonts/04b03b.ttf");
    }

    /**
     * 该方法用于获取字体
     * @param fontName：字体名称
     * @return 字体名称对应的字体
     */
    public Typeface getTypeface(String fontName) {
        switch (fontName) {
            case "7thc":
                return _7thc;
            case "7thi":
                return _7thi;
            case "04b24":
                return _04b24;
            case "04b03b":
                return _04b03b;
            default:
                return Typeface.DEFAULT;
        }
    }

    /**
     * 该方法将 dp 转换成 px
     * @param dpValue：dp 值
     * @return px 值
     */
    public int dpToPx(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
