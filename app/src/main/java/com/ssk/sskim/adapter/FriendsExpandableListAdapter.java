package com.ssk.sskim.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jxmpp.util.XmppStringUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ssk.sskim.R;
import com.ssk.sskim.beans.FriendInfo;
import com.ssk.sskim.beans.GroupInfo;
import com.ssk.sskim.utils.ImgUtils;
import com.ssk.sskim.view.CircleImageView;

public class FriendsExpandableListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater inflater;
	
	//联系人分组集合
	private List<GroupInfo> groupData;

	//联系人集合（一个分组下多个联系人）
	private List<List<FriendInfo>> friendsData;

	/**
	 * 
	 * @param context
	 * @param groups 好友列表的所有分组
	 */
	public FriendsExpandableListAdapter(Context context, Collection<RosterGroup> groups, Set<RosterEntry> stranger) {
		inflater = LayoutInflater.from(context);
		setData(null,null,groups,stranger);
	}
	
	/**
	 * 获取所有的好友数据（便利所有的分组，然后再遍历分组下的所有的RosterEntry）
	 * @param groups
	 */
	public void setData(AbstractXMPPConnection connection, Roster roster, Collection<RosterGroup> groups, Set<RosterEntry> stranger){
		//注意：在这里初始胡
		groupData = new ArrayList<GroupInfo>();
		friendsData = new ArrayList<List<FriendInfo>>();
		List<FriendInfo> strangerList = new ArrayList<FriendInfo>();
		//陌生人
		if(stranger!=null){
			for (RosterEntry item: stranger){
				FriendInfo friend = new FriendInfo();
				String nickname = XmppStringUtils.parseLocalpart(item.getUser());
				friend.setJid(item.getUser());
				//用户名
				friend.setUsername(nickname);
				//名称
				friend.setName(nickname);
				//心情
				friend.setMood("陌生人请求加好友");
				friend.setType(Presence.Type.unavailable);
				strangerList.add(friend);
			}
		}
		for (RosterGroup group : groups) {
			//分组
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setGroupName(group.getName());
			groupData.add(groupInfo);
			//获取分组下的所有好友
			List<RosterEntry> entries = group.getEntries();
			List<FriendInfo> friendsList = new ArrayList<FriendInfo>();
			for (RosterEntry entry : entries) {
				//双方都是好友，才显示到列表中
				if (TextUtils.equals("both", entry.getType().name())) {
					FriendInfo friend = new FriendInfo();
					friend.setJid(entry.getUser());
					//用户名
					friend.setUsername(entry.getName());
					//名称
					friend.setName(entry.getName());
					if(roster!=null){
						Presence presence=roster.getPresence(entry.getUser());
						if(presence.getType()== Presence.Type.unavailable){
							friend.setMood("离线");
						}else{
							friend.setMood("在线");
						}
						if(connection!=null){
							VCardManager vm=VCardManager.getInstanceFor(connection);
							try {
								VCard v=vm.loadVCard(entry.getUser());
								Bitmap bitmap=ImgUtils.Bytes2Bimap(v.getAvatar());
								if(bitmap!=null){
									friend.setPhoto(new SoftReference<Bitmap>(bitmap));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					friendsList.add(friend);
					if(strangerList.size()>0){
						//取消已经添加的好友
						List<FriendInfo> dellist=new ArrayList<>();
						  for (FriendInfo item:strangerList){
							  if(item.getJid()==friend.getJid()){
								  dellist.add(item);
							  }
						  }
						  if(dellist.size()>0){
							  for (FriendInfo item:dellist){
								  strangerList.remove(item);
							  }
						  }
					}
				}
			}
			groupInfo.setFriends(friendsList);
			friendsData.add(friendsList);
		}
		if(strangerList.size()>0){
			GroupInfo groupInfo2 = new GroupInfo();
			groupInfo2.setGroupName("陌生人");
			groupData.add(groupInfo2);
			groupInfo2.setFriends(strangerList);
			friendsData.add(strangerList);
		}
	}
	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	//获取对应分组下的所有好友个数
	@Override
	public int getChildrenCount(int groupPosition) {
		List<FriendInfo> list = friendsData.get(groupPosition);
		if (list != null && !list.isEmpty()) {
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return friendsData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	//联系人分组条目
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View layout = inflater.inflate(R.layout.friend_group_item, null);
		TextView tv_group_name = (TextView) layout.findViewById(R.id.tv_group_name);
		//设置组名
		GroupInfo group = (GroupInfo) getGroup(groupPosition);
		tv_group_name.setText(group.getGroupName());
		
		ImageView iv_group_icon = (ImageView) layout.findViewById(R.id.iv_group_icon);
		if (isExpanded) {
			iv_group_icon.setBackgroundResource(R.drawable.sc_group_expand);
		}else{
			iv_group_icon.setBackgroundResource(R.drawable.sc_group_unexpand);
		}
		return layout;
	}

	//联系人条目
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		View layout = inflater.inflate(R.layout.friend_child_item, null);
		FriendInfo friend = (FriendInfo) getChild(groupPosition, childPosition);
		
		//昵称
		TextView tv_friend_nickname = (TextView) layout.findViewById(R.id.tv_friend_nickname);
		tv_friend_nickname.setText(friend.getName());
		
		//心情
		TextView tv_friend_mood = (TextView) layout.findViewById(R.id.tv_friend_mood);
		tv_friend_mood.setText(friend.getMood());

		CircleImageView iv_friend_icon = (CircleImageView) layout.findViewById(R.id.iv_friend_icon);

		if(friend.getPhoto()!=null){
			iv_friend_icon.setImageBitmap(friend.getPhoto().get());
		}
		return layout;
	}

	
	//子条目可以选择
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public FriendInfo getItemByJid(String jid){
		for (List<FriendInfo> list:friendsData){
			for (FriendInfo item:list){
				if(jid.equals(item.getJid())){
					return  item;
				}
			}
		}
		return null;
	}

}
