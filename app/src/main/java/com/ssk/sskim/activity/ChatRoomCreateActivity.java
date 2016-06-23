package com.ssk.sskim.activity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jxmpp.util.XmppStringUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ssk.sskim.R;
import com.ssk.sskim.manager.ChatRoomManager;
import com.ssk.sskim.manager.ConnectionManager;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 创建群
 */
public class ChatRoomCreateActivity extends BaseToolBarActivity {

	private EditText et_name;
	private EditText et_password;
	private EditText et_description;
	@Override
	protected int getContentView() {
		return R.layout.chatroom_create;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		et_name = (EditText) findViewById(R.id.et_name);
		et_password = (EditText) findViewById(R.id.et_password);
		et_description = (EditText) findViewById(R.id.et_description);
	}
	/**
	 * 创建房间
	 * @param btn
	 */
	public void createChatRoom(View btn) {
		new CreateRoomTask().execute(et_name.getText().toString(),et_password.getText().toString(),et_description.getText().toString());
	}

	class CreateRoomTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			try {
				AbstractXMPPConnection connection = ConnectionManager.getConnection();
				// 房间名称
				String name = params[0];
				// 进入的密码
				String password = params[1];
				// 描述
				String description = params[2];
				// 群昵称
				String nickname = XmppStringUtils.parseLocalpart(connection.getUser());
				//创建立即加入
				if(!ChatRoomManager.isChatRoom(connection,name)){
					ChatRoomManager.createRoom(connection, name, password, description);
					ChatRoomManager.addBookmarkedRoom(connection, name, nickname, password);
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
				Toast.makeText(ChatRoomCreateActivity.this, "群创建成功！", Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_OK);
				finish();
			}if(result==2){
				Toast.makeText(ChatRoomCreateActivity.this, "群已存在！", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(ChatRoomCreateActivity.this, "群创建失败！", Toast.LENGTH_SHORT).show();
			}
		}

	}

}
