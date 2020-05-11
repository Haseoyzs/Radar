package cn.haseo.radar.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 该类用于监听传感器
 */
public class SensorListener implements SensorEventListener{
    // 定义传感器
    private Sensor sensor;
    // 定义传感器管理者
    private SensorManager sensorManager;
    // 定义方向监听器
    private OrientationListener listener;

    // 定义方向传感器 X 轴的最新数值
    private float lastX;


    /**
     * 该方法设置方向监听器
     * @param listener：方向监听接口
     */
    public void setOrientationListener(OrientationListener listener) {
        this.listener = listener;
    }


    /**
     * 构造方法
     * @param context：传感器的上下文
     */
    @SuppressWarnings("deprecation")
    public SensorListener(Context context) {
        // 获取传感器管理者的实例
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // 获取方向传感器的实例
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }


    /**
     * 该方法在传感器数值发生变化时调用
     * @param event：传感器事件的事件变量
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onSensorChanged(SensorEvent event) {
        // 当发生数值变化的传感器是方向传感器时
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            // 只获取方向传感器 X 轴的值
            float currX = event.values[SensorManager.DATA_X];

            // 当方向传感器 X 轴的数值变化幅度大于 1.0 时
            if(Math.abs(currX - lastX) > 1.0f){
                if(listener != null){
                    // 回调接口传送传感器 X 轴的数值
                    listener.onOrientationChanged(currX);
                }
            }

            // 保存方向传感器当前 X 轴的数值
            lastX = currX;
        }
    }

    /**
     * 该方法在传感器精度发生改变时调用（这里用不上）
     * @param sensor：传感器
     * @param accuracy：传感器精度
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    /**
     * 该方法启动监听传感器数值变化
     */
    public void register(){
        if (sensorManager != null && sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * 该方法取消监听传感器数值变化
     */
    public void unregister(){
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }


    /**
     * 自定义方向监听接口
     */
    public interface OrientationListener {
        void onOrientationChanged(float currX);
    }
}