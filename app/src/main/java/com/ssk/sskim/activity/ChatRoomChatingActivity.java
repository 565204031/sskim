package com.ssk.sskim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ssk.sskim.R;
import com.ssk.sskim.adapter.ChatListAdapter;
import com.ssk.sskim.beans.ChatRoom;
import com.ssk.sskim.beans.DbMessage;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.manager.ChatRoomManager;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.service.MessageService;
import com.ssk.sskim.utils.Constants;
import com.ssk.sskim.utils.DbUtils;
import com.ssk.sskim.utils.TimeUtils;
import com.ssk.sskim.view.RecordButton;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.util.XmppStringUtils;
import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 群聊
 */
public class ChatRoomChatingActivity extends BindActivity  {

	private List<IMessage> msgList = new ArrayList<IMessage>();
	private EditText et_msg;
	private ChatListAdapter adapter;
	private String loggedUser;
	private MultiUserChat muc;
	private FileTransferManager fileTransferManager;
	
	private static final int MESSAGE_REFRESH_CHAT_LIST = 1;

	private DbManager db;

	private String chattoJid;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			//刷新消息列表
			case MESSAGE_REFRESH_CHAT_LIST:
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	};
	@Override
	protected int getContentView() {
		return R.layout.chatroom_chating;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		et_msg = (EditText) findViewById(R.id.et_msg);
		db = x.getDb(DbUtils.daoConfig);

		Intent intent = getIntent();
		//当前所在的群
		chattoJid = intent.getStringExtra("chattoJid");

		AbstractXMPPConnection connection = ConnectionManager.getConnection();
		//当前登录用户
		loggedUser = XmppStringUtils.parseBareJid(connection.getUser());

		ListView lv_msg = (ListView) findViewById(R.id.lv_msg);
		adapter = new ChatListAdapter(this, msgList);
		lv_msg.setAdapter(adapter);
		initData();
	}
	private void initData() {
		try {
			List<DbMessage> list;
			list=db.selector(DbMessage.class).where(WhereBuilder.b("jid","==",chattoJid).and("uid","==",loggedUser).and("read","==","0")).findAll();
			if(list!=null){
				for (DbMessage item:list){
					msgList.add(IMessage.toParse(item.getContent()));
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		adapter.notifyDataSetChanged();
	}
	public void mSend(View btn){
		String content = et_msg.getText().toString();
		//用户要立马看到自己发出的这条消息显示到消息列表
		IMessage localMsg = new IMessage(loggedUser, content, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT);
		//添加到消息列表，刷新ListView
		msgList.add(localMsg);
		adapter.notifyDataSetChanged();
		
		//这条消息才是真的要发送出去的消息
		//在对方看来，这条消息的消息类型是IN
		IMessage remoteMsg = new IMessage(loggedUser, content, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN);
		
		try {
			//转为json文本发送出去
			muc.sendMessage(remoteMsg.toJson());
			mMessageService.addDb(chattoJid,localMsg.toJson(),true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//清空输入框
		et_msg.setText("");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_room_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.member:
			   //群成员
				break;
			case R.id.room:
				//聊天记录
				Bundle bundle=new Bundle();
				bundle.putString("chattoJid",chattoJid);
				bundle.putString("uid",loggedUser);
				BaseActivity.start(getContext(),ChatHistoryActivity.class,bundle);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	void onBindFinish(MessageService service) {

	}
	@Override
	void onChatRoomMessage(String jid, String txt) {
		if(jid.equals(chattoJid)){
			msgList.add(IMessage.fromJson(txt));
			//刷新
			handler.sendEmptyMessage(MESSAGE_REFRESH_CHAT_LIST);
			try {
				db.update(DbMessage.class, WhereBuilder.b("jid","==",jid).and("uid","==",loggedUser),new KeyValue("read","1"));
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}
}
