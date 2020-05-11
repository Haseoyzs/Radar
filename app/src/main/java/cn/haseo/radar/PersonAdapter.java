package cn.haseo.radar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import org.litepal.LitePal;

import cn.haseo.radar.model.Person;
import cn.haseo.radar.widget.MyTextView;

/**
 * 人物适配器
 */
public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {
    // 定义 RecyclerView 的上下文
    private Context context;
    // 定义人物集合
    private List<Person> persons;

    // 用于标记是否可以编辑 RecyclerView 列表
    private boolean editable = false;
    // 定义 RecyclerView 子项点击事件的接口变量
    private OnItemClickListener listener;


    /**
     * 该方法获取子项的可编辑状态
     */
    boolean isEditable() {
        return editable;
    }

    /**
     * 该方法设置子项的可编辑状态
     * @param editable：子项可编辑状态
     */
    void setEditable(boolean editable) {
        this.editable = editable;
    }
    /**
     * 该方法设置 RecyclerView 子项点击事件的监听器
     * @param listener：子项点击事件的接口变量
     */
    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    /**
     * 构造方法
     * @param context：RecyclerView 的上下文
     * @param persons：人物集合
     */
    PersonAdapter(Context context, List<Person> persons) {
        this.context = context;
        this.persons = persons;
    }


    @Override
    public int getItemCount() {
        return persons.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.person_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    @SuppressWarnings("all")
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        // 获取子项对应的人物
        final Person person = persons.get(i);

        // 监听子项的点击事件
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(person);
            }
        });

        // 当人物是好友时
        if (person.isFriend()) {
            // 设置好友的图标
            viewHolder.personIcon.setImageResource(R.drawable.friend_icon);
            // 设置好友昵称的字体颜色
            viewHolder.personName.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            // 当人物是敌人时，设置敌人的图标
            viewHolder.personIcon.setImageResource(R.drawable.enemy_icon);
            // 设置敌人昵称的字体颜色
            viewHolder.personName.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        viewHolder.personName.setText(person.getName());

        // 当 RcyclerView 列表可编辑时
        if (editable) {
            // 显示删除按钮
            viewHolder.deletePerson.setVisibility(View.VISIBLE);
            // 监听删除按钮的点击事件
            viewHolder.deletePerson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 加载对话框布局
                    final View dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_delete_person, null);

                    // 获取对话框标题实例
                    MyTextView dialogTitle = dialogLayout.findViewById(R.id.dialog_title);
                    // 获取对话框昵称标签实例
                    MyTextView nameLabel = dialogLayout.findViewById(R.id.name_label);
                    // 获取对话框号码标签实例
                    MyTextView numberLabel = dialogLayout.findViewById(R.id.number_label);

                    // 设置人物名称
                    ((TextView) dialogLayout.findViewById(R.id.person_name)).setText(person.getName());
                    // 设置人物号码
                    ((MyTextView) dialogLayout.findViewById(R.id.person_number)).setText(person.getNumber());

                    // 当人物是好友时
                    if (person.isFriend()) {
                        // 设置对话框对应的标题
                        dialogTitle.setText(R.string.delete_friend_title);
                        // 设置对话框昵称标签的字体颜色
                        nameLabel.setTextColor(ContextCompat.getColor(context, R.color.green));
                        // 设置对话框号码标签的字体颜色
                        numberLabel.setTextColor(ContextCompat.getColor(context, R.color.green));
                    } else {
                        // 当人物是敌人时，设置对话框对应的标题
                        dialogTitle.setText(R.string.delete_enemy_title);
                        // 设置对话框昵称标签的字体颜色
                        nameLabel.setTextColor(ContextCompat.getColor(context, R.color.red));
                        // 设置对话框号码标签的字体颜色
                        numberLabel.setTextColor(ContextCompat.getColor(context, R.color.red));
                    }

                    // 创建对话框
                    final AlertDialog dialog = new AlertDialog.Builder(context, R.style.alertDialogTheme).setCancelable(true).show();

                    // 获取对话框的窗体
                    Window window = dialog.getWindow();
                    // 设置对话框窗体的布局
                    window.setContentView(dialogLayout);

                    // 设置窗体高度
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.height = App.getInstance().dpToPx(240);
                    window.setAttributes(layoutParams);

                    // 监听确定按钮的点击事件
                    dialogLayout.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 删除对应对应号码的人物
                            LitePal.deleteAll(Person.class, "number = ?", person.getNumber());
                            // 标记人物数据已更改
                            App.getInstance().setDataChange(true);
                            // 关闭对话框
                            dialog.dismiss();
                            // 从数据源中删除对应人物
                            persons.remove(person);
                            // 刷新列表
                            notifyDataSetChanged();
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
                }
            });
        } else {
            // 隐藏删除按钮
            viewHolder.deletePerson.setVisibility(View.GONE);
        }
    }


    /**
     * 自定义 ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        // 定义 RecyclerView 子项
        private View itemView;
        // 定义人物图标
        private ImageView personIcon;
        // 定义人物昵称
        private TextView personName;
        // 定义删除按钮
        private ImageButton deletePerson;

        /**
         * 构造方法
         * @param itemView：子项
         */
        ViewHolder(View itemView) {
            super(itemView);
            // 获取子项实例
            this.itemView = itemView;
            // 获取人物图标实例
            personIcon = itemView.findViewById(R.id.person_icon);
            // 获取人物昵称实例
            personName = itemView.findViewById(R.id.person_name);
            // 获取删除按钮实例
            deletePerson = itemView.findViewById(R.id.delete_person);
        }
    }

    /**
     * 定义 RecyclerView 子项点击事件的监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(Person person);
    }
}
