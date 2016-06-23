package com.ssk.sskim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ssk.sskim.R;
import com.ssk.sskim.beans.ChatRoom;

import java.util.List;

public class ChatRoomListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<ChatRoom> roomList;
	private Context context;
	
	public ChatRoomListAdapter(Context context,List<ChatRoom> roomList) {
		super();
		setData(roomList);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}
	
	public void setData(List<ChatRoom> roomList){
		this.roomList = roomList;
	}

	@Override
	public int getCount() {
		return roomList.size();
	}

	@Override
	public Object getItem(int position) {
		return roomList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatRoom info = roomList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.room_list_item, null);
		}
		TextView tv_name = (TextView)convertView.findViewById(R.id.tv_name);
		tv_name.setText(info.getName());
		
		TextView tv_description = (TextView)convertView.findViewById(R.id.tv_description);
		tv_description.setText(info.getDescription());
		
		TextView tv_occupants = (TextView)convertView.findViewById(R.id.tv_occupants);
		tv_occupants.setText(String.valueOf(info.getOccupantsCount()));
		
		return convertView;
	}

}
