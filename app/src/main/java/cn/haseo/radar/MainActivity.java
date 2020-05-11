package cn.haseo.radar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.litepal.LitePal;

import cn.haseo.radar.model.Person;
import cn.haseo.radar.util.DesUtil;
import cn.haseo.radar.util.SensorListener;
import cn.haseo.radar.util.SendMsgUtil;

/**
 * 主活动
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // 获取 Application 实例
    private App app = App.getInstance();

    // 定义声音池
    private SoundPool soundPool;
    // 定义声音资源 id
    private int soundId;

    // 用于标志是否是第一次定位
    private boolean isFirstLocate = true;

    // 定义扫描线图片
    private ImageView scanLine;
    // 定义地图控件
    private MapView mapView;
    // 定义地图的总控制器
    private BaiduMap baiduMap;
    // 定义定位服务客户端
    private LocationClient locationClient;

    // 定义我的位置
    private BDLocation myLocation;
    // 定义地图文本集合
    private List<Text> texts = new ArrayList<>();
    // 定义地图折线集合
    private List<Polyline> polylines = new ArrayList<>();
    
    // 定义传感器监听器
    private SensorListener sensorListener;
    // 定义方向传感器 X 轴当前的数值
    private float currX;

    // 定义人物集合
    private List<Person> people = new ArrayList<>();
    // 用于标记是否对某人开启追踪
    private boolean isTrack = false;

    // 定义短信发送工具
    private SendMsgUtil sendMsgUtil;
    // 定义短信的观察者
    private SmsObserver smsObserver;
    // 定义 Handler 变量
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // 往地图中添加点标记
                addMarker();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 当系统版本大于等于 6.0 时
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 申请运行时权限
            requestPermission();
        }

        // 初始化声音池（5.0 及以上系统采用新 API 构造方法）
        soundPool = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new SoundPool.Builder().setMaxStreams(1).build() : new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // 获取声音资源 id
        soundId = soundPool.load(this, R.raw.sweep_sound, 1);
        // 获取扫描线图片的实例
        scanLine = findViewById(R.id.scan_line);

        // 监听定位按钮的点击事件
        findViewById(R.id.locate).setOnClickListener(this);
        // 监听刷新按钮的点击事件
        findViewById(R.id.refresh).setOnClickListener(this);

        // 获取前往好友列表的按钮实例
        Button goFriendList = findViewById(R.id.go_friend_list);
        goFriendList.setTypeface(app.getTypeface("7thc"));
        goFriendList.setOnClickListener(this);
        // 获取前往敌人列表的按钮实例
        Button goEnemyList = findViewById(R.id.go_enemy_list);
        goEnemyList.setTypeface(app.getTypeface("7thc"));
        goEnemyList.setOnClickListener(this);

        // 初始化定位配置
        initBaiduMap();

        // 创建传感器监听器
        sensorListener = new SensorListener(this);
        // 监听手机方向变化事件
        sensorListener.setOrientationListener(new SensorListener.OrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                // 将方向传感器 X 轴当前的值赋给全局变量 currX
                currX = x;
            }
        });


        // 获取人物集合
        people = LitePal.findAll(Person.class);
        // 往地图中添加点标记
        addMarker();

        // 创建短信发送工具
        sendMsgUtil = new SendMsgUtil(this);
        // 创建短信的观察者
        smsObserver = new SmsObserver(handler);
    }

    /**
     * 该方法用于申请权限
     */
    private void requestPermission() {
        // 创建权限集合
        List<String> permissions = new ArrayList<>();
        // 申请获取位置的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // 申请获取手机状态的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        // 申请获取读扩展存储的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // 申请发送短信的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS);
        }
        // 申请读取短信的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS);
        }
        // 当权限集合不为空时，申请权限
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 1);
        }
    }

    /**
     * 该方法初始化百度地图
     */
    @SuppressWarnings("all")
    private void initBaiduMap() {
        // 获取地图控件实例
        mapView = findViewById(R.id.map_view);
        // 不显示地图缩放控件
        mapView.showZoomControls(false);
        // 不显示地图上比例尺
        mapView.showScaleControl(false);

        // 初始化定位服务客户端
        locationClient = new LocationClient(app);
        // 监听我的位置变化事件
        locationClient.registerLocationListener(new MyLocationListener());
        // 创建定位客户端的设置
        LocationClientOption options = new LocationClientOption();
        // 设置使用高精度定位模式
        options.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 设置使用 GPS 定位模式
        options.setOpenGps(true);
        // 设置返回的经纬度坐标类型
        options.setCoorType("bd09ll");
        // 设置发起定位请求的间隔时间
        options.setScanSpan(1000);
        // 设置需要地址信息
        options.setIsNeedAddress(true);
        // 设置需要描述位置
        options.setIsNeedLocationDescribe(true);
        // 保存设置到定位客户端
        locationClient.setLocOption(options);

        // 获取地图总控制器
        baiduMap = mapView.getMap();
        // 监听地图点的击事件
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 当没有开启追踪某人时
                if (!isTrack) {
                    // 隐藏信息窗
                    baiduMap.hideInfoWindow();
                }
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        // 监听点标记的点击事件
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // 获取点标记对应的人物
                final Person person = LitePal.where("number = ? ", marker.getTitle()).find(Person.class).get(0);

                // 获取信息窗各控件的实例
                View infoWindow = LayoutInflater.from(MainActivity.this).inflate(R.layout.info_windows, null);
                TextView nameInfo = infoWindow.findViewById(R.id.name_info);
                TextView numberInfo = infoWindow.findViewById(R.id.number_info);
                final Button track = infoWindow.findViewById(R.id.track);
                Button detail = infoWindow.findViewById(R.id.detail);

                // 设置各控件对应的字体颜色
                if (person.isFriend()) {
                    int green = Color.parseColor("#006400");
                    nameInfo.setTextColor(green);
                    numberInfo.setTextColor(green);
                    track.setTextColor(green);
                    detail.setTextColor(green);
                } else {
                    int red = Color.parseColor("#EE0000");
                    nameInfo.setTextColor(red);
                    numberInfo.setTextColor(red);
                    track.setTextColor(red);
                    detail.setTextColor(red);
                }

                // 设置信息窗对应的人物名字
                nameInfo.setText(person.getName());
                // 设置信息窗对应的人物号码
                numberInfo.setText(person.getNumber());

                // 监听追踪按钮的点击事件
                track.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 当关闭追踪某人时
                        if (isTrack) {
                            // 将追踪标记设为 false
                            isTrack = false;
                            // 还原追踪按钮默认的颜色
                            track.setTextColor(person.isFriend() ? Color.parseColor("#006400") : Color.parseColor("#EE0000"));
                            // 隐藏信息窗
                            baiduMap.hideInfoWindow();
                        } else {
                            // 当开启追踪某人时，将追踪标记设为 true
                            isTrack = true;
                            // 将追踪按钮的字体颜色设为黑色
                            track.setTextColor(Color.BLACK);
                        }
                    }
                });
                // 监听详情按钮的点击事件
                detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 隐藏信息窗
                        baiduMap.hideInfoWindow();
                        // 创建 Intent
                        Intent intent = new Intent(MainActivity.this, person.isFriend() ? FriendDetailActivity.class : EnemyDetailActivity.class);
                        intent.putExtra("number", marker.getTitle());
                        // 跳转到对应的详情页
                        startActivity(intent);
                    }
                });
                // 添加信息窗到点标记
                baiduMap.showInfoWindow(new InfoWindow(infoWindow, marker.getPosition(), -app.dpToPx(20.0f)));
                return true;
            }
        });
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
    }

    /**
     * 该方法用于添加地图标记
     */
    private void addMarker() {
        // 清除地图上的覆盖物
        baiduMap.clear();
        // 根据人物创建对应的点标记
        for (Person person : people) {
            // 当人物信息的更新时间不为空时
            if (person.getLastUpdated() != null) {
                // 创建点标记
                MarkerOptions option = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(person.isFriend() ? R.drawable.friend_icon : R.drawable.enemy_icon))
                        .position(new LatLng(person.getLatitude(), person.getLongitude()))
                        .animateType(MarkerOptions.MarkerAnimateType.drop)
                        .title(person.getNumber());
                // 添加点标记到地图
                baiduMap.addOverlay(option);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // 启动定位服务客户端
        if (!locationClient.isStarted()) {
            locationClient.start();
        }

        // 监听短信数据库是否改变
        getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox"), true, smsObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 继续地图控件
        mapView.onResume();

        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);

        // 监听方向传感器
        sensorListener.register();

        // 监听短信发送结果
        sendMsgUtil.register();

        // 当人物数据发生改变时
        if (app.isDataChange()) {
            // 重新获取人物数据
            people = LitePal.findAll(Person.class);
            // 重新添加地图标记
            addMarker();
            // 取消标记人物数据已更改
            app.setDataChange(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停地图控件
        mapView.onPause();

        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);

        // 取消监听方向传感器
        sensorListener.unregister();

        // 取消监听短信发送结果
        sendMsgUtil.unregister();
    }

    @Override
    protected void onStop() {
        // 关闭定位服务客户端
        if(locationClient.isStarted()){
            locationClient.stop();
        }

        // 取消监听短信数据库是否改变
        getContentResolver().unregisterContentObserver(smsObserver);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 清除地图数据
        baiduMap.clear();
        mapView.onDestroy();
        baiduMap = null;
        mapView = null;

        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 当点击定位按钮时
            case R.id.locate:
                // 回到我的位置
                if (myLocation != null) {
                    baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
                }
                break;
            // 当点击刷新按钮时
            case R.id.refresh:
                // 回到我的位置
                if (myLocation != null) {
                    baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
                }
                // 扫描线转动
                scanLine.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_anim));
                // 播放声音
                soundPool.play(soundId, 1, 1, 1, 5, 1);
                // 向每个人物发送位置请求
                for (Person person : people) {
                    sendMsgUtil.sendMessage(person.getNumber(), "LOC");
                }
                break;
            // 当点击前往敌人列表的按钮时
            case R.id.go_enemy_list:
                startActivity(new Intent(MainActivity.this, EnemyListActivity.class));
                break;
            // 当点击前往好友列表的按钮时
            case R.id.go_friend_list:
                startActivity(new Intent(MainActivity.this, FriendListActivity.class));
                break;
        }
    }

    /**
     * 该方法处理按键的按下的事件
     * @param keyCode：按键键码值
     * @param event：按键事件变量
     * @return true 不让事件继续向下传递，false 表示让事件继续向下传递
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当触发事件的按键是返回键时
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 让主活动退到后台，false 表示只对主活动有效
            moveTaskToBack(false);
            // 不让事件继续向下传递
            return true;
        }
        // 调用父类的方法继续处理返回键事件
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 该方法用于回调授权结果
     * @param requestCode：请求码
     * @param permissions：权限集合
     * @param grantResults：授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            // 检查授权结果
            for (int result : grantResults) {
                // 当有一项权限没有获取时
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // 弹出提示
                    Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                    // 结束活动
                    finish();
                    return;
                }
            }
        } else {
            Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * 该类用于监听短信数据库是否发生改变
     */
    private class SmsObserver extends ContentObserver {
        /**
         * 构造方法
         * @param handler：handler 变量
         */
        SmsObserver(Handler handler) {
            super(handler);
        }

        /**
         * 当短信数据库发生改变时调用该方法
         * @param selfChange：该值一般为 false，意义不大
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // 取出短信
            getSmsFromPhone();
        }

        /**
         * 该方法用于读取手机短信
         */
        @SuppressWarnings("all")
        private void getSmsFromPhone() {
            // 定义消息
            Message msg = new Message();
            // 定义光标
            Cursor cursor = null;

            // 按号码检索信息库
            for (Person person : people) {
                try {
                    // 按号码获取对应人物 10 分钟收到的短信
                    cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{ "address", "body" }, "date > ? and address = ?",
                            new String[]{ String.valueOf(System.currentTimeMillis() - 600000), person.getNumber() }, "date desc");
                    if (cursor != null && cursor.moveToFirst()) {
                        // 取出短信内容
                        String body = cursor.getString(cursor.getColumnIndex("body"));

                        // 当收到的是位置请求短信时
                        if (body.equals("LOC")) {
                            // 构建位置数据
                            String location = myLocation.getLatitude() + " " + myLocation.getLongitude() + " " + myLocation.getAltitude() + " " +
                                    myLocation.getRadius() + " " + myLocation.getAddrStr();
                            // 发送加密过的位置信息给对方
                            sendMsgUtil.sendMessage(person.getNumber(), "Location:" + DesUtil.encryptData(location));
                        } else if (body.contains("Location:")) {
                            // 当收到的是位置密文短信时，从位置密文短信中解析出位置数据
                            String[] location = DesUtil.decryptData(body.substring(9, body.length())).split(" ");
                            // 按照解析出来的位置数据更新对应人物的各项位置信息
                            person.setLatitude(Double.parseDouble(location[0]));
                            person.setLongitude(Double.parseDouble(location[1]));
                            person.setAltitude(Double.parseDouble(location[2]));
                            person.setAccuracy(Double.parseDouble(location[3]));
                            person.setAddress(location[4]);
                            person.setLastUpdated(new Date());
                            person.updateAll("number = ? ", person.getNumber());
                            // 标记需要更新地图上的点标记
                            msg.what = 1;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 当光标不为空时
                    if (cursor != null) {
                        // 关闭光标
                        cursor.close();
                    }
                }
            }
            handler.sendMessage(msg);
        }
    }

    /**
     * 该类用于监听自己得位置变化
     */
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            super.onConnectHotSpotMessage(s, i);
        }
        @Override
        public void onLocDiagnosticMessage(int i, int i1, String s) {
            super.onLocDiagnosticMessage(i, i1, s);
        }
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // 当采用 GPS 定位或者 网络定位时
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 导航到自己得位置
                updateLocation(bdLocation);
            }
        }

        /**
         * 该方法用于实时更新位置信息
         * @param bdLocation：位置参数
         */
        private void updateLocation(BDLocation bdLocation) {
            // 第一次定位时
            if (isFirstLocate) {
                // 使用动画缩放地图
                baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(17.0f));
                // 使用动画更新位置
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude())));
                // 第一次定位结束
                isFirstLocate = false;
            }

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(currX)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);

            // 清除地图上的线
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines.clear();

            // 清除地图上的文本
            for (Text text : texts) {
                text.remove();
            }
            texts.clear();

            // 获取我的经纬度
            LatLng mLatlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            // 将我与各人物进行连线并标识距离
            for (Person person : people) {
                if (person.getLastUpdated() != null) {
                    // 获取人物的经纬度
                    LatLng fLatlng = new LatLng(person.getLatitude(), person.getLongitude());

                    // 绘制线
                    List<LatLng> points = new ArrayList<>();
                    points.add(mLatlng);
                    points.add(fLatlng);
                    PolylineOptions ooPolyline = new PolylineOptions()
                            .width(8)
                            .points(points);

                    // 绘制文本
                    TextOptions textOption = new TextOptions()
                            .position(new LatLng((bdLocation.getLatitude() + person.getLatitude()) / 2, (bdLocation.getLongitude() + person.getLongitude()) / 2))
                            .text(String.format(getResources().getString(R.string.distance), DistanceUtil.getDistance(mLatlng, fLatlng) / 1000))
                            .typeface(app.getTypeface("04b24"))
                            .fontSize(app.dpToPx(18.0f))
                            .bgColor(Color.TRANSPARENT);

                    // 为线和文本设置对应的颜色
                    if (person.isFriend()) {
                        ooPolyline.color(0xFF05EC09);
                        textOption.fontColor(0xFF006400);
                    } else {
                        ooPolyline.color(0xFFEB3037);
                        textOption.fontColor(0xFFCD0000);
                    }

                    // 添加线到地图
                    polylines.add((Polyline) baiduMap.addOverlay(ooPolyline));
                    // 添加文字到地图
                    texts.add((Text) baiduMap.addOverlay(textOption));
                }
            }

            // 记录我的位置
            myLocation = bdLocation;
        }
    }
}
