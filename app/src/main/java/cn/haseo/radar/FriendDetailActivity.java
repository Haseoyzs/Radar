package cn.haseo.radar;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.litepal.LitePal;

import cn.haseo.radar.model.Person;
import cn.haseo.radar.widget.MyTextView;

/**
 * 好友详情活动
 */
public class FriendDetailActivity extends AppCompatActivity implements View.OnClickListener {
    // 获取 Application 实例
    private App app = App.getInstance();
    // 定义好友
    private Person friend;
    // 定义编辑好友的按钮
    private ImageButton editFriend;
    // 定义删除好友的按钮
    private ImageButton deleteFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        // 获取资源
        Resources res = getResources();
        // 获取号码对应的好友
        friend = LitePal.where("number = ?", getIntent().getStringExtra("number")).find(Person.class).get(0);


        // 设置标题栏的背景颜色
        findViewById(R.id.detail_header).setBackgroundResource(R.drawable.friend_header_bg);

        // 获取返回好友列表的按钮实例
        ImageButton backList = findViewById(R.id.back_list);
        // 设置按钮的图标
        backList.setImageResource(R.drawable.friend_list_go_btn);
        // 监听按钮的点击事件
        backList.setOnClickListener(this);

        // 设置标题栏的标题
        ((MyTextView) findViewById(R.id.detail_title)).setText(R.string.text_friend);

        // 获取编辑好友按钮的实例
        editFriend = findViewById(R.id.edit_person);
        // 设置按钮的图标
        editFriend.setImageResource(R.drawable.friend_list_edit_btn);
        // 监听按钮的点击事件
        editFriend.setOnClickListener(this);


        // 设置好友图标
        ((ImageView) findViewById(R.id.person_icon)).setImageResource(R.drawable.friend_icon);

        // 获取显示好友昵称的标签实例
        TextView friendName = findViewById(R.id.person_name);
        // 设置标签的字体颜色
        friendName.setTextColor(ContextCompat.getColor(this, R.color.green));
        // 显示好友昵称
        friendName.setText(friend.getName());

        // 获取删除好友按钮的实例
        deleteFriend =  findViewById(R.id.delete_person);
        // 监听按钮的点击事件
        deleteFriend.setOnClickListener(this);


        // 显示好友的号码
        ((MyTextView) findViewById(R.id.person_number)).setText(friend.getNumber());
        // 当好友已更新过位置数据时
        if (friend.getLastUpdated() != null) {
            // 显示好友的经纬度
            ((MyTextView) findViewById(R.id.person_latlng)).setText(String.format(res.getString(R.string.latlng), friend.getLatitude(), friend.getLongitude()));
            // 显示好友的海拔
            ((MyTextView) findViewById(R.id.person_altitude)).setText(String.format(res.getString(R.string.altitude), friend.getAltitude()));
            // 显示好友的定位精度
            ((MyTextView) findViewById(R.id.person_accuracy)).setText(String.format(res.getString(R.string.accuracy), friend.getAccuracy()));
            // 显示好友的地址
            ((TextView) findViewById(R.id.person_address)).setText(friend.getAddress());
            // 显示上一次更新好友位置的时间
            ((MyTextView) findViewById(R.id.lastUpdated)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(friend.getLastUpdated()));
        }


        // 获取返回雷达的按钮的实例
        Button backRadar = findViewById(R.id.go_radar);
        // 设置按钮的字体
        backRadar.setTypeface(app.getTypeface("7thc"));
        // 监听按钮的点击事件
        backRadar.setOnClickListener(this);

        // 获取前往敌人列表的按钮实例
        Button goEnemyList = findViewById(R.id.go_list);
        // 设置按钮的字体
        goEnemyList.setTypeface(app.getTypeface("7thc"));
        // 设置按钮的字体颜色
        goEnemyList.setTextColor(ContextCompat.getColor(this, R.color.red));
        // 设置按钮的文本
        goEnemyList.setText(R.string.text_enemy);
        // 监听按钮的点击事件
        goEnemyList.setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("all")
    public void onClick(View v) {
        switch (v.getId()) {
            // 当点击返回好友列表的按钮时
            case R.id.back_list:
                // 跳转到好友列表
                startActivity(new Intent(this, FriendListActivity.class));
                break;
            // 当点击编辑好友的按钮时
            case R.id.edit_person:
                // 当删除好友的按钮不显示时
                if (deleteFriend.getVisibility() == View.GONE) {
                    // 更改按钮的 Edit 图标为 Done 图标
                    editFriend.setImageResource(R.drawable.friend_list_done_btn);
                    // 显示删除好友的按钮
                    deleteFriend.setVisibility(View.VISIBLE);
                } else {
                    // 当删除好友的按钮显示时，更改按钮的 Done 图标为 Edit 图标
                    editFriend.setImageResource(R.drawable.friend_list_edit_btn);
                    // 隐藏删除好友的按钮
                    deleteFriend.setVisibility(View.GONE);
                }
                break;
            // 当点击删除好友的按钮时
            case R.id.delete_person:
                // 获取对话框的布局
                final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_delete_person, null);
                // 设置对话框的标题
                ((MyTextView) dialogLayout.findViewById(R.id.dialog_title)).setText(R.string.delete_friend_title);
                // 设置对话框标签的字体颜色
                ((MyTextView) dialogLayout.findViewById(R.id.name_label)).setTextColor(ContextCompat.getColor(this, R.color.green));
                ((MyTextView) dialogLayout.findViewById(R.id.number_label)).setTextColor(ContextCompat.getColor(this, R.color.green));
                // 显示被删好友的昵称与号码
                ((TextView) dialogLayout.findViewById(R.id.person_name)).setText(friend.getName());
                ((MyTextView) dialogLayout.findViewById(R.id.person_number)).setText(friend.getNumber());

                // 创建对话框
                final AlertDialog dialog = new AlertDialog.Builder(this, R.style.alertDialogTheme).setCancelable(true).show();

                // 获取对话框的窗体
                Window window = dialog.getWindow();
                // 设置对话框窗体的布局
                window.setContentView(dialogLayout);

                // 设置对话框窗体的高度
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.height = app.dpToPx(240);
                window.setAttributes(layoutParams);

                // 监听确定按钮的点击事件
                dialogLayout.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除对应对应号码的好友
                        LitePal.deleteAll(Person.class, "number = ?", friend.getNumber());
                        // 标记人物数据已更改
                        app.setDataChange(true);
                        // 关闭窗口
                        dialog.dismiss();
                        // 结束活动
                        finish();
                    }
                });

                // 监听取消按钮的点击事件
                dialogLayout.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 关闭窗口
                        dialog.dismiss();
                    }
                });
                break;
            // 当点击返回雷达的按钮时
            case R.id.go_radar:
                // 结束活动
                finish();
                break;
            // 当点击前往敌人列表的按钮时
            case R.id.go_list:
                // 跳转到敌人列表
                startActivity(new Intent(this, EnemyListActivity.class));
                break;
        }
    }
}
