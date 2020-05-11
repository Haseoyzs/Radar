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

public class EnemyDetailActivity extends AppCompatActivity implements View.OnClickListener {
    // 获取 Application 实例
    private App app = App.getInstance();
    // 定义敌人
    private Person enemy;
    // 定义编辑敌人的按钮
    private ImageButton editEnemy;
    // 定义删除敌人的按钮
    private ImageButton deleteEnemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enemy_details);

        // 获取资源
        Resources res = getResources();
        // 获取号码对应的敌人
        enemy = LitePal.where("number = ?", getIntent().getStringExtra("number")).find(Person.class).get(0);


        // 设置标题栏的背景颜色
        findViewById(R.id.detail_header).setBackgroundResource(R.drawable.enemy_header_bg);

        // 获取返回敌人列表的按钮实例
        ImageButton backList = findViewById(R.id.back_list);
        // 设置按钮的图标
        backList.setImageResource(R.drawable.enemy_list_go_btn);
        // 监听按钮的点击事件
        backList.setOnClickListener(this);

        // 设置标题栏的标题
        ((MyTextView) findViewById(R.id.detail_title)).setText(R.string.text_enemy);

        // 获取编辑敌人按钮的实例
        editEnemy = findViewById(R.id.edit_person);
        // 设置按钮的图标
        editEnemy.setImageResource(R.drawable.enemy_list_edit_btn);
        // 监听按钮的点击事件
        editEnemy.setOnClickListener(this);


        // 设置好友图标
        ((ImageView) findViewById(R.id.person_icon)).setImageResource(R.drawable.enemy_icon);

        // 获取显示敌人昵称的标签实例
        TextView enemyName = findViewById(R.id.person_name);
        // 设置标签的字体颜色
        enemyName.setTextColor(ContextCompat.getColor(this, R.color.red));
        // 显示好友昵称
        enemyName.setText(enemy.getName());

        // 获取删除敌人按钮的实例
        deleteEnemy = findViewById(R.id.delete_person);
        // 监听按钮的点击事件
        deleteEnemy.setOnClickListener(this);


        // 显示敌人的号码
        ((MyTextView) findViewById(R.id.person_number)).setText(enemy.getNumber());
        // 当敌人已经更新过位置数据时
        if (enemy.getLastUpdated() != null) {
            // 显示敌人的经纬度
            ((MyTextView) findViewById(R.id.person_latlng)).setText(String.format(res.getString(R.string.latlng), enemy.getLatitude(), enemy.getLongitude()));
            // 显示敌人的海拔
            ((MyTextView) findViewById(R.id.person_altitude)).setText(String.format(res.getString(R.string.altitude), enemy.getAltitude()));
            // 显示敌人的定位精度
            ((MyTextView) findViewById(R.id.person_accuracy)).setText(String.format(res.getString(R.string.accuracy), enemy.getAccuracy()));
            // 显示敌人的地址
            ((TextView) findViewById(R.id.person_address)).setText(enemy.getAddress());
            // 显示上一次更新敌人位置的时间
            ((MyTextView) findViewById(R.id.lastUpdated)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(enemy.getLastUpdated()));
        }


        // 获取返回雷达的按钮的实例
        Button goRadar = findViewById(R.id.go_radar);
        // 设置按钮的字体
        goRadar.setTypeface(app.getTypeface("7thc"));
        // 监听按钮的点击事件
        goRadar.setOnClickListener(this);


        // 获取前往好友列表的按钮实例
        Button goFriendList = findViewById(R.id.go_list);
        // 设置按钮的字体
        goFriendList.setTypeface(app.getTypeface("7thc"));
        // 设置按钮的字体颜色
        goFriendList.setTextColor(ContextCompat.getColor(this, R.color.green));
        // 设置按钮的文本
        goFriendList.setText(R.string.text_friend);
        // 监听按钮的点击事件
        goFriendList.setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("all")
    public void onClick(View v) {
        switch (v.getId()) {
            // 当点击返回敌人列表的按钮时
            case R.id.back_list:
                // 跳转到敌人列表
                startActivity(new Intent(this, EnemyListActivity.class));
                break;
            // 当点击编辑敌人的按钮时
            case R.id.edit_person:
                // 当删除敌人的按钮不显示时
                if (deleteEnemy.getVisibility() == View.GONE) {
                    // 更改按钮的 Edit 图标为 Done 图标
                    editEnemy.setImageResource(R.drawable.enemy_list_done_btn);
                    // 显示删除敌人的按钮
                    deleteEnemy.setVisibility(View.VISIBLE);
                } else {
                    // 当删除敌人的按钮显示时，更改按钮的 Done 图标为 Edit 图标
                    editEnemy.setImageResource(R.drawable.enemy_list_edit_btn);
                    // 隐藏删除敌人的按钮
                    deleteEnemy.setVisibility(View.GONE);
                }
                break;
            // 当点击删除敌人的按钮时
            case R.id.delete_person:
                // 加载对话框的布局
                final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_delete_person, null);
                // 设置对话框的标题
                ((MyTextView) dialogLayout.findViewById(R.id.dialog_title)).setText(R.string.delete_enemy_title);
                // 设置对话框标签的字体颜色
                ((MyTextView) dialogLayout.findViewById(R.id.name_label)).setTextColor(ContextCompat.getColor(this, R.color.red));
                ((MyTextView) dialogLayout.findViewById(R.id.number_label)).setTextColor(ContextCompat.getColor(this, R.color.red));
                // 显示被删敌人的昵称与号码
                ((TextView) dialogLayout.findViewById(R.id.person_name)).setText(enemy.getName());
                ((MyTextView) dialogLayout.findViewById(R.id.person_number)).setText(enemy.getNumber());

                // 创建对话框
                final AlertDialog dialog = new AlertDialog.Builder(this, R.style.alertDialogTheme).setCancelable(true).show();

                // 获取对话框的窗体
                Window window = dialog.getWindow();
                // 设置对话框窗体的布局
                window.setContentView(dialogLayout);

                // 设置对话框窗体的高度
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.height = App.getInstance().dpToPx(240);
                window.setAttributes(layoutParams);

                // 监听确定按钮的点击事件
                dialogLayout.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除对应对应号码的敌人
                        LitePal.deleteAll(Person.class, "number = ?", enemy.getNumber());
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
            // 当点击前往好友列表的按钮时
            case R.id.go_list:
                // 跳转到好友列表
                startActivity(new Intent(this, FriendListActivity.class));
                break;
        }
    }
}
