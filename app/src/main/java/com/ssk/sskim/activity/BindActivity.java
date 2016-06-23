package com.ssk.sskim.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ssk.sskim.manager.SubscribeManager;
import com.ssk.sskim.service.MessageService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.util.XmppStringUtils;

import de.measite.minidns.record.A;

/**
 * Created by 杀死凯 on 2016/5/29.
 * 绑定服务基类
 */
public abstract class BindActivity extends BaseToolBarActivity implements ServiceConnection {

    private MessageReceiver receiver;
    MessageService mMessageService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        intent.setClass(this, MessageService.class);
        //this.startService(intent);//启动服务
        this.bindService(intent, this, Context.BIND_AUTO_CREATE);

        //注册广播接收者
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.ACTION_MESSAGE);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MessageService.MyBinder binder = (MessageService.MyBinder) service;
        mMessageService=binder.getService();
        onBindFinish(mMessageService);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    abstract  void  onBindFinish(MessageService service);

    /**
     * 广播接收者
     */
    private class  MessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int type= (int) intent.getExtras().get("type");
            String data= (String) intent.getExtras().get("data");
            String jid=  (String) intent.getExtras().get("jid");
            switch (type){
                case MessageService.TYPE_CHATMESSAGE:
                    onChatMessage(jid,data);
                    break;
                case MessageService.TYPE_CHATSTATE:
                    onChatState(jid,data);
                    break;
                case MessageService.TYPE_PRESENC_ADD:
                    //添加好友告知
                    onAddFriend(jid,data);
                    break;
                case MessageService.TYPE_FILE_FINISH:
                    //文件传输完成回调
                    onFileFinish(data);
                    break;
                case MessageService.TYPE_CHATROOMMESSAGE:
                    //群聊天
                    onChatMessage(jid,data);
                    break;
            }
        }
    }
    void onChatMessage(String jid,String txt){

    }
    void onChatState(String jid,String state){

    }
    void onAddFriend(String jid,String name){
        alertInvestDialog(jid);
    }

    void onFileFinish(String name){

    }
    void onChatRoomMessage(String jid,String txt){

    }
    protected void alertInvestDialog(final String inverstorJid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("好友申请");
        builder.setMessage("【"+inverstorJid+"】向你发来好友申请，是否添加对方为好友？");
        builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //添加到“我的好友”组
                String nickname = XmppStringUtils.parseLocalpart(inverstorJid);
                mMessageService.getRosterManager().addEntry(inverstorJid, nickname, "我的好友");
                //通知请求的好友，你的请求已经通过了
                SubscribeManager.subscribed(mMessageService.getConnection(),inverstorJid);

            }
        });
        builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //请求不通过
                Roster roster  =mMessageService.getRoster();
                RosterEntry entry = roster.getEntry(inverstorJid);
                try {
                    roster.removeEntry(entry);
                    SubscribeManager.unsubscribe(mMessageService.getConnection(),inverstorJid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
