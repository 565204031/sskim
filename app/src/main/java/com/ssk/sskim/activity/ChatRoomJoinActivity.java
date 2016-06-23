package com.ssk.sskim.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.ssk.sskim.R;
import com.ssk.sskim.manager.ChatRoomManager;
import com.ssk.sskim.manager.ConnectionManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jxmpp.util.XmppStringUtils;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 加入群
 */
public class ChatRoomJoinActivity extends BaseToolBarActivity {
	
	private EditText et_name;
	private EditText et_password;
	private AbstractXMPPConnection connection;

	@Override
	protected int getContentView() {
		return R.layout.chatroom_join;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		et_name = (EditText) findViewById(R.id.et_name);
		et_password = (EditText) findViewById(R.id.et_password);
		connection = ConnectionManager.getConnection();
	}

	/**
	 * 加入群聊
	 * @param btn
	 */
	public void join(View btn){
		new CreateJoinTask().execute(et_name.getText().toString(),et_password.getText().toString());
	}
	
	class CreateJoinTask extends AsyncTask<String, Void,Integer> {

		private String name;
		private String password;
		private String nickname;

		@Override
		protected Integer doInBackground(String... params) {
			try {
				// 房间名称
				name = params[0];
				// 进入的密码
				password =params[1];
				// 进群之后穿的马甲
				nickname = XmppStringUtils.parseLocalpart(connection.getUser());
				if(ChatRoomManager.isChatRoom(connection,name)){
					ChatRoomManager.joinRoom(connection, name, password, nickname);
				}else{
					return 2;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result==1) {
				Toast.makeText(ChatRoomJoinActivity.this, "加入群组成功！", Toast.LENGTH_SHORT).show();
				//收藏群组
				ChatRoomManager.addBookmarkedRoom(connection, name, nickname, password);
				setResult(Activity.RESULT_OK);
				finish();
			}else if(result==2){
				Toast.makeText(ChatRoomJoinActivity.this, "群组不存在！", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(ChatRoomJoinActivity.this, "加入群组失败！", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
}
