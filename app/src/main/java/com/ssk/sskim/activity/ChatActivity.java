package com.ssk.sskim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.util.XmppStringUtils;
import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ssk.sskim.R;
import com.ssk.sskim.adapter.ChatListAdapter;
import com.ssk.sskim.beans.DbMessage;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.service.MessageService;
import com.ssk.sskim.utils.Constants;
import com.ssk.sskim.utils.DbUtils;
import com.ssk.sskim.utils.ImgUtils;
import com.ssk.sskim.utils.PictureUploading;
import com.ssk.sskim.utils.TimeUtils;
import com.ssk.sskim.utils.UUIDUtils;
import com.ssk.sskim.view.RecordButton;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 单人聊天
 */
public class ChatActivity extends BindActivity implements RecordButton.OnRecordFinishedListener {

	private AbstractXMPPConnection connection = ConnectionManager.getConnection();
	
	private EditText et_msg;

	private ChatManager chatManager;

	private Chat chat;
	
	private List<IMessage> msgList = new ArrayList<IMessage>();

	private ChatListAdapter adapter;

	private String loggedUser;

	private String chattoJid;

	private DbManager db;

	private static final int MESSAGE_REFRESH_CHAT_LIST = 1;

	private boolean isInput;


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
		return R.layout.chat;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		db = x.getDb(DbUtils.daoConfig);
		et_msg = (EditText) findViewById(R.id.et_msg);
		RecordButton btn_record = (RecordButton) findViewById(R.id.btn_record);
		btn_record.setOnRecordFinishedListener(this);
		Intent intent = getIntent();
		//当前的聊天对象
		chattoJid = intent.getStringExtra("chattoJid");

		setTitle(chattoJid);
		//当前登录用户
		loggedUser = XmppStringUtils.parseBareJid(connection.getUser());
		ListView lv_msg = (ListView) findViewById(R.id.lv_msg);
		adapter = new ChatListAdapter(this, msgList);
		lv_msg.setAdapter(adapter);

//		//聊天管理器
//		chatManager = ChatManager.getInstanceFor(connection);
//		//监听对话的创建
//		chatManager.addChatListener(this);
//		//--------------注意这两段代码的顺序
//		//创建一个对话
//		chat = chatManager.createChat(chattoJid);
//		//文件传输管理器
//		fileTransferManager = FileTransferManager.getInstanceFor(connection);
//		//注册监听，收文件
//		fileTransferManager.addFileTransferListener(this);
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
		if(btn.getId()==R.id.btn_img){
			//发送图片
			PictureUploading.showSelect(this, 0);
		}else{
			//发送文字
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
				chat.sendMessage(remoteMsg.toJson());
				mMessageService.addDb(chattoJid,localMsg.toJson(),true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//清空输入框
			et_msg.setText("");
		}
	}
	
	/**
	 * 根据文件名，找到对应的消息，然后更新消息的状态
	 * @param name
	 */
	private void updateMsgStatus(String name) {
		updateMsgStatus(name,IMessage.MESSAGE_STATUS_SUCCESS);
	}

	/**
	 * 根据文件名，找到对应的消息，然后更新消息的状态
	 * @param name
	 * @param status
     */
	private void updateMsgStatus(String name,int status) {
		for (IMessage msg : msgList) {
			if (name.equals(msg.getFileName())) {
				msg.setStatus(status);
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public void onFinished(File audioFile, int duration) {
		if (audioFile == null) {
			return;
		}
		//localMsg
		IMessage localMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT, duration, audioFile.getName());
		msgList.add(localMsg);
		adapter.notifyDataSetChanged();
		//remoteMsg
		IMessage remoteMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN, duration, audioFile.getName());
		try {
			//转为json文本发送出去
			chat.sendMessage(remoteMsg.toJson());
			mMessageService.addDb(chattoJid,localMsg.toJson(),true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//传输文件
		OutgoingFileTransfer transfer = mMessageService.getFileTransferManager().createOutgoingFileTransfer(chattoJid+"/Smack");
		try {
			transfer.sendFile(audioFile, remoteMsg.toJson());
			//更新消息的状态
			updateMsgStatus(audioFile.getName());
		} catch (SmackException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.history:
				//聊天记录
				Bundle bul=new Bundle();
				bul.putString("chattoJid",chattoJid);
				bul.putString("uid",	connection.getUser());
				BaseActivity.start(getContext(),ChatHistoryActivity.class,bul);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onBindFinish(MessageService service) {
		chat = service.getChatManager().createChat(chattoJid);
	}

	@Override
	void onChatMessage(String jid, String txt){
		if(jid.equals(chattoJid)){
			msgList.add(IMessage.fromJson(txt));
			//刷新
			handler.sendEmptyMessage(MESSAGE_REFRESH_CHAT_LIST);
			try {
				db.update(DbMessage.class,WhereBuilder.b("jid","==",jid).and("uid","==",loggedUser),new KeyValue("read","1"));
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	void onChatState(String jid, String state) {
		if(jid.equals(chattoJid)){
			if ("composing".equals(state)){
				setTitle("正在输入...");
			}else{
				setTitle(chattoJid);
			}
		}
	}

	@Override
	void onFileFinish(String name) {
		updateMsgStatus(name);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PictureUploading.onActivityResult(this, new PictureUploading.OnBackCall() {

			@Override
			public void onSucceed(Bitmap bmp, int index) {
				//收到图片成功
				File imgFile=ImgUtils.save( UUIDUtils.generate(),bmp);
				if(imgFile!=null){
					//localMsg
					IMessage localMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT, imgFile.getName());
					msgList.add(localMsg);
					adapter.notifyDataSetChanged();
					//remoteMsg
					IMessage remoteMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN, imgFile.getName());
					try {
						//转为json文本发送出去
						chat.sendMessage(remoteMsg.toJson());
						mMessageService.addDb(chattoJid,localMsg.toJson(),true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//传输文件
					OutgoingFileTransfer transfer = mMessageService.getFileTransferManager().createOutgoingFileTransfer(chattoJid+"/Smack");
					try {
						transfer.sendFile(imgFile, remoteMsg.toJson());
						//更新消息的状态
						updateMsgStatus(imgFile.getName());
					} catch (SmackException e) {
						e.printStackTrace();
					}
				}
			}
			@Override
			public void onFail() {
				Toast.makeText(getContext(),"图片参数错误",Toast.LENGTH_SHORT).show();
			}
		}, requestCode, resultCode, data);
	}
}
