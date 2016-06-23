package com.ssk.sskim.adapter;



import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ssk.sskim.R;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.utils.AudioUtils;
import com.ssk.sskim.utils.Constants;

import java.io.File;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<IMessage> msgList;

	public ChatListAdapter(Context context, List<IMessage> msgList) {
		super();
		this.context = context;
		this.msgList = msgList;
		this.inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final IMessage message = msgList.get(position);
		// 收到的消息
		if (message.getMsgType() == IMessage.MESSAGE_TYPE_IN) {
			convertView = inflater.inflate(R.layout.formclient_chat_in, null);
		} else {
			convertView = inflater.inflate(R.layout.formclient_chat_out, null);
		}
		TextView tv_sender = (TextView) convertView.findViewById(R.id.tv_sender);
		tv_sender.setText(message.getSender());

		TextView tv_date = (TextView) convertView.findViewById(R.id.tv_date);
		tv_date.setText(message.getDate());

		TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);
		tv_content.setText(message.getContent());

		TextView tv_status = (TextView) convertView.findViewById(R.id.tv_status);

		ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_img);


		if(message.getStatus()==IMessage.MESSAGE_STATUS_WAIT){
			//等待
			if(!(message.getMsgModle()==IMessage.MESSAGE_MODEL_TEXT)){
				String path="";
				if(message.getMsgModle()==IMessage.MESSAGE_MODEL_IMG){
					path = Constants.IMG_DIR.getAbsolutePath() + "/" + message.getFileName();
				}else if(message.getMsgModle()==IMessage.MESSAGE_MODEL_AUDIO){
					path = Constants.AUDIO_DIR.getAbsolutePath() + "/" + message.getFileName();
				}
				File file= new File(path);
				if(file==null){
					tv_status.setText("加载中");
				}
			}
		}else if(message.getStatus()==IMessage.MESSAGE_STATUS_SUCCESS){
			if(message.getMsgModle()==IMessage.MESSAGE_MODEL_IMG){
				iv_img.setVisibility(View.VISIBLE);
				iv_img.setImageBitmap(BitmapFactory.decodeFile(Constants.IMG_DIR.getAbsolutePath()+"/"+message.getFileName()));
			}
		}
		//语音消息，注册点击事件监听
		if (message.getMsgModle() == IMessage.MESSAGE_MODEL_AUDIO) {
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//发送成功点击才有效
					//播放声音
					String path = Constants.AUDIO_DIR.getAbsolutePath() + "/" + message.getFileName();
					File file= new File(path);
					if(file!=null){
						AudioUtils.play(path, context);
					}else{
						Toast.makeText(context,"正在加载，请稍后",Toast.LENGTH_SHORT).show();
					}
				}
			});
		}else{
			convertView.setOnClickListener(null);
		}
		return convertView;
	}

}
