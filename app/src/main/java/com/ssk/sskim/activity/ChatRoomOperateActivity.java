package com.ssk.sskim.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.muc.RoomInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ssk.sskim.R;
import com.ssk.sskim.adapter.ChatRoomListAdapter;
import com.ssk.sskim.beans.ChatRoom;
import com.ssk.sskim.manager.ChatRoomManager;
import com.ssk.sskim.manager.ConnectionManager;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 群相关操作
 */
public class ChatRoomOperateActivity extends BaseToolBarActivity implements OnItemClickListener {

	private List<ChatRoom> roomList = new ArrayList<ChatRoom>();
	private ChatRoomListAdapter adapter;
	private AbstractXMPPConnection connection;

	@Override
	protected int getContentView() {
		return R.layout.chatroom_operate;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		connection = ConnectionManager.getConnection();
		ListView lv_rooms = (ListView) findViewById(R.id.lv_rooms);
		adapter = new ChatRoomListAdapter(this,roomList);
		lv_rooms.setAdapter(adapter);
		lv_rooms.setOnItemClickListener(this);

		new ChatRoomLoader().execute();
	}

	/**
	 * 创建群聊
	 * @param layout
	 */
	public void createRoom(View layout){
		Intent intent = new Intent(this,ChatRoomCreateActivity.class);
		startActivityForResult(intent, 100);
	}
	
	/**
	 * 加入群聊
	 * @param btn
	 */
	public void joinRoom(View btn){
		Intent intent = new Intent(this,ChatRoomJoinActivity.class);
		startActivityForResult(intent, 200);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//刷新群列表
		new ChatRoomLoader().execute();
	}
	
	
	class ChatRoomLoader extends AsyncTask<Void, Void, List<ChatRoom>>{

		@Override
		protected List<ChatRoom> doInBackground(Void... params) {
			//等待服务器更新
			SystemClock.sleep(1000);
			//获取已经加入了的房间
			return ChatRoomManager.getBookmarkedRooms(connection);
		}
		
		@Override
		protected void onPostExecute(List<ChatRoom> result) {
			if (result != null) {
				adapter.setData(result);
				adapter.notifyDataSetChanged();
			}
		}
		
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//点击条目之后，跳转到群聊界面
		Intent intent = new Intent(this,ChatRoomChatingActivity.class);
		ChatRoom chatroom = (ChatRoom) adapter.getItem(position);
		intent.putExtra("chattoJid",chatroom.getJid());
		startActivity(intent);
	}
	
}
