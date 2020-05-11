package cn.haseo.radar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import cn.haseo.radar.model.Person;
import cn.haseo.radar.widget.MyTextView;

/**
 * 敌人列表活动
 */
public class EnemyListActivity extends AppCompatActivity implements View.OnClickListener, PersonAdapter.OnItemClickListener {
    // 获取 Application 实例
    private App app = App.getInstance();
    // 定义敌人集合
    private List<Person> enemies = new ArrayList<>();
    // 定义人物适配器
    private PersonAdapter adapter;
    // 定义编辑敌人的按钮
    private ImageButton editEnemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        // 获取敌人集合
        enemies = LitePal.where("isFriend = ?", "0").find(Person.class);


        // 设置标题栏的背景颜色
        findViewById(R.id.list_header).setBackgroundResource(R.drawable.enemy_header_bg);

        // 获取添加敌人按钮的实例
        ImageButton addEnemy = findViewById(R.id.add_person);
        // 设置按钮的图标
        addEnemy.setImageResource(R.drawable.enemy_list_add_btn);
        // 监听按钮的点击事件
        addEnemy.setOnClickListener(this);

        // 设置标题栏的标题
        ((MyTextView) findViewById(R.id.list_title)).setText(R.string.text_enemy);

        // 获取编辑好友按钮的实例
        editEnemy = findViewById(R.id.edit_person);
        // 设置按钮的图标
        editEnemy.setImageResource(R.drawable.button_enemy_list_edit);
        // 监听按钮的点击事件
        editEnemy.setOnClickListener(this);


        // 获取 RecyclerView 的实例
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 设置 RecyclerView 的布局方式
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 创建人物适配器
        adapter = new PersonAdapter(this, enemies);
        // 监听 RecyclerView 子项的点击事件
        adapter.setOnItemClickListener(this);
        // 设置 RecyclerView 的适配器
        recyclerView.setAdapter(adapter);


        // 获取返回雷达的按钮实例
        Button goRadar = findViewById(R.id.go_radar);
        // 设置按钮的字体
        goRadar.setTypeface(app.getTypeface("7thc"));
        // 监听按钮的点击事件
        goRadar.setOnClickListener(this);

        // 获取前往敌人列表的按钮实例
        Button goFriendList = findViewById(R.id.go_list);
        // 设置按钮的字体
        goFriendList.setTextColor(ContextCompat.getColor(this, R.color.green));
        // 设置按钮的字体颜色
        goFriendList.setText(R.string.text_friend);
        // 设置按钮的文本
        goFriendList.setTypeface(app.getTypeface("7thc"));
        // 监听按钮的点击事件
        goFriendList.setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("all")
    public void onClick(View v) {
        switch(v.getId()) {
            // 当点击添加敌人的按钮时
            case R.id.add_person:
                // 加载对话框的布局
                final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null);
                // 设置对话框的标题
                ((MyTextView) dialogLayout.findViewById(R.id.dialog_title)).setText(R.string.add_enemy_title);
                // 设置对话框标签的字体颜色
                ((MyTextView) dialogLayout.findViewById(R.id.name_label)).setTextColor(ContextCompat.getColor(this, R.color.red));
                ((MyTextView) dialogLayout.findViewById(R.id.number_label)).setTextColor(ContextCompat.getColor(this, R.color.red));

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

                // 弹出软件盘
                window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                // 监听确定按钮的点击事件
                dialogLayout.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 创建数据库
                        Connector.getDatabase();

                        // 获取输入框中敌人的名字
                        Editable friendName = ((EditText) dialogLayout.findViewById(R.id.input_name)).getText();
                        // 获取输入框中敌人的号码
                        Editable friendNumber = ((EditText) dialogLayout.findViewById(R.id.input_number)).getText();

                        // 当两个输入框中都有数据时
                        if (!TextUtils.isEmpty(friendName) && !TextUtils.isEmpty(friendNumber)) {
                            // 根据号码查询敌人是否已保存
                            List<Person> results = LitePal.where("number = ?", friendNumber.toString()).find(Person.class);

                            // 若敌人已保存则获取否则创建
                            Person enemy = results.isEmpty() ? new Person() : results.get(0);
                            // 保存或更新敌人的数据
                            enemy.setName(friendName.toString());
                            enemy.setNumber(friendNumber.toString());
                            enemy.setFriend(false);
                            enemy.save();

                            // 更新敌人列表
                            enemies.clear();
                            enemies.addAll(LitePal.where("isFriend = ?", "0").find(Person.class));
                            adapter.notifyDataSetChanged();
                            // 标记人物数据已改变
                            app.setDataChange(true);

                            // 关闭对话框
                            dialog.dismiss();
                        } else {
                            // 当数据不完整时，弹出提示
                            Toast.makeText(EnemyListActivity.this, "请将信息填写完整", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // 监听取消按钮的点击事件
                dialogLayout.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 关闭对话框
                        dialog.dismiss();
                    }
                });
                break;
            // 当点击编辑敌人的按钮时
            case R.id.edit_person:
                // 当敌人列表处于可编辑状态时
                if (adapter.isEditable()) {
                    // 将敌人列表设为不可编辑状态
                    adapter.setEditable(false);
                    // 设置对应的图标
                    editEnemy.setSelected(false);
                } else {
                    // 当敌人列表处于不可编辑状态时，将列表设为可编辑状态
                    adapter.setEditable(true);
                    // 设置对应的图标
                    editEnemy.setSelected(true);
                }
                // 刷新敌人列表
                adapter.notifyDataSetChanged();
                break;
            // 当点击返回雷达按钮时
            case R.id.go_radar:
                // 结束活动
                finish();
                break;
            // 当点击前往好友列表的按钮时
            case R.id.go_list:
                // 前往好友列表
                startActivity(new Intent(this, FriendListActivity.class));
                break;
        }
    }

    /**
     * 该方法处理子项的点击事件
     * @param person：子项对应的人物
     */
    @Override
    public void onItemClick(Person person) {
        // 创建前往详情页的 Intent
        Intent intent = new Intent(this, EnemyDetailActivity.class);
        // 添加敌人号码到 Intent
        intent.putExtra("number", person.getNumber());
        // 跳转到详情页
        startActivity(intent);
    }
}
