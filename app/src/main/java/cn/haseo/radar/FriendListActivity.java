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
 * 好友列表活动
 */
public class FriendListActivity extends AppCompatActivity implements View.OnClickListener, PersonAdapter.OnItemClickListener {
    // 获取 Application 实例
    private App app = App.getInstance();
    // 定义好友集合
    private List<Person> friends = new ArrayList<>();
    // 定义人物适配器
    private PersonAdapter adapter;
    // 定义编辑好友的按钮
    private ImageButton editFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        // 获取好友集合
        friends = LitePal.where("isFriend = ?", "1").find(Person.class);


        // 设置标题栏的背景颜色
        findViewById(R.id.list_header).setBackgroundResource(R.drawable.friend_header_bg);

        // 获取添加好友按钮的实例
        ImageButton addFriend = findViewById(R.id.add_person);
        // 设置按钮的图标
        addFriend.setImageResource(R.drawable.friend_list_add_btn);
        // 监听按钮的点击事件
        addFriend.setOnClickListener(this);

        // 设置标题栏的标题
        ((MyTextView) findViewById(R.id.list_title)).setText(R.string.text_friend);

        // 获取编辑好友的按钮的实例
        editFriend = findViewById(R.id.edit_person);
        // 设置按钮的图标
        editFriend.setImageResource(R.drawable.button_friend_list_edit);
        // 监听按钮的点击事件
        editFriend.setOnClickListener(this);


        // 获取 RecyclerView 的实例
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 设置 RecyclerView 的布局方式
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 创建人物适配器
        adapter = new PersonAdapter(this, friends);
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
        switch(v.getId()) {
            // 当点击添加好友的按钮时
            case R.id.add_person:
                // 加载对话框的布局
                final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null);
                // 设置对话框的标题
                ((MyTextView)dialogLayout.findViewById(R.id.dialog_title)).setText(R.string.add_friend_title);
                // 设置对话框标签的字体颜色
                ((MyTextView)dialogLayout.findViewById(R.id.name_label)).setTextColor(ContextCompat.getColor(this, R.color.green));
                ((MyTextView)dialogLayout.findViewById(R.id.number_label)).setTextColor(ContextCompat.getColor(this, R.color.green));

                // 创建对话框
                final AlertDialog dialog = new AlertDialog.Builder(this, R.style.alertDialogTheme).setCancelable(true).show();

                // 获取对话的框窗体
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

                        // 获取输入框中好友的名字
                        Editable friendName = ((EditText) dialogLayout.findViewById(R.id.input_name)).getText();
                        // 获取输入框中好友的号码
                        Editable friendNumber = ((EditText) dialogLayout.findViewById(R.id.input_number)).getText();

                        // 当两个输入框都有数据时
                        if (!TextUtils.isEmpty(friendName) && !TextUtils.isEmpty(friendNumber)) {
                            // 根据号码查询好友是否已保存
                            List<Person> results = LitePal.where("number = ?", friendNumber.toString()).find(Person.class);

                            // 若好友已保存则获取否则创建
                            Person friend = results.isEmpty() ? new Person() : results.get(0);
                            // 保存或更新好友的数据
                            friend.setName(friendName.toString());
                            friend.setNumber(friendNumber.toString());
                            friend.setFriend(true);
                            friend.save();

                            // 更新好友列表
                            friends.clear();
                            friends.addAll(LitePal.where("isFriend = ?", "1").find(Person.class));
                            adapter.notifyDataSetChanged();
                            // 标记人物数据已改变
                            app.setDataChange(true);

                            // 关闭对话框
                            dialog.dismiss();
                        } else {
                            // 当数据不完整时，弹出提示
                            Toast.makeText(FriendListActivity.this, "请将人物信息填写完整", Toast.LENGTH_SHORT).show();
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
            // 当点击编辑好友的按钮时
            case R.id.edit_person:
                // 当好友列表处于可编辑状态时
                if (adapter.isEditable()) {
                    // 将好友列表设为不可编辑状态
                    adapter.setEditable(false);
                    editFriend.setSelected(false);
                } else {
                    // 当好友列表处于不可编辑状态时，将列表设为可编辑状态
                    adapter.setEditable(true);
                    editFriend.setSelected(true);
                }
                // 刷新好友列表
                adapter.notifyDataSetChanged();
                break;
            // 当点击返回雷达的按钮时
            case R.id.go_radar:
                // 结束活动
                finish();
                break;
            // 当点击前往敌人列表的按钮时
            case R.id.go_list:
                // 前往敌人列表
                startActivity(new Intent(this, EnemyListActivity.class));
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
        Intent intent = new Intent(this, FriendDetailActivity.class);
        // 添加好友号码到 Intent
        intent.putExtra("number", person.getNumber());
        // 前往详情页
        startActivity(intent);
    }
}
