package cn.haseo.radar.util;

import java.util.ArrayList;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * 该类用于发送短信
 */
public class SendMsgUtil {
    // 定义上下文
    private Context context;
    // 定义短信发送成功与否的广播接收器
    private SendResultReceiver sendResultReceiver;
    // 定义对方接收成功与否的广播接收器
    private ReceiveResultReceiver receiveResultReceiver;


    /**
     * 构造方法
     * @param context：上下文
     */
    public SendMsgUtil(Context context) {
        // 初始化上下文
        this.context = context;
        // 创建短信发送成功与否的广播接收器
        sendResultReceiver = new SendResultReceiver();
        // 创建对方接收成功与否的广播接收器
        receiveResultReceiver = new ReceiveResultReceiver();
    }


    /**
     * 该方法实现发送短信的功能
     * @param number：手机号码
     * @param content：短信内容
     */
    public void sendMessage(String number, String content) {
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent("SENT_SMS_ACTION"), 0);
        PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, new Intent("DELIVERED_SMS_ACTION"), 0);

        SmsManager smsManager = SmsManager.getDefault();
        // 如果短信字数超过 70 时
        if (content.length() > 70) {
            // 拆分成多条短信发送（对方收到的是一条完整短信）
            ArrayList<String> messages = smsManager.divideMessage(content);
            ArrayList<PendingIntent> sentIntents =  new ArrayList<>();
            for(int i = 0; i < messages.size(); i++){
                sentIntents.add(sentPI);
            }
            smsManager.sendMultipartTextMessage(number, null, messages, sentIntents, null);
        } else {
            smsManager.sendTextMessage(number, null, content, sentPI, deliverPI);
        }
    }


    /**
     * 注册广播接收器
     */
    public void register() {
        context.registerReceiver(sendResultReceiver, new IntentFilter("SENT_SMS_ACTION"));
        context.registerReceiver(receiveResultReceiver, new IntentFilter("DELIVERED_SMS_ACTION"));
    }

    /**
     * 取消注册广播接收器
     */
    public void unregister() {
        context.unregisterReceiver(sendResultReceiver);
        context.unregisterReceiver(receiveResultReceiver);
    }


    /**
     * 该广播用于提示短信的发送结果
     */
    private class SendResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断短信是否发送成功
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "短信发送失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 该广播用于提示对方是否接收到短信
     */
    private class ReceiveResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 表示对方成功收到短信
            Toast.makeText(context, "对方接收成功", Toast.LENGTH_SHORT).show();
        }
    }
}