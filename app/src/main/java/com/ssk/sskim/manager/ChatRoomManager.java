package com.ssk.sskim.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.util.XmppStringUtils;

import android.os.SystemClock;

import com.ssk.sskim.beans.ChatRoom;

public class ChatRoomManager {

	public static final String CONFERENCE = "@conference."; 
	
	/**
	 * 创建一个房间(群组)用于群聊
	 * @param connection
	 * @param name 群组的名字
	 * @param password 密码
	 * @param description 群组的描述
	 * @return
	 * @throws Exception
	 */
	public static MultiUserChat createRoom(AbstractXMPPConnection connection,String name,String password,String description) throws Exception{
		//群聊管理器
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		//群聊对话
		//房间（群组）的jid，区分用户jid
		String jid = name + CONFERENCE + connection.getServiceName();
		MultiUserChat muc = manager.getMultiUserChat(jid);
		muc.createOrJoin(name);
		//对房间进行配置
		Form configForm = muc.getConfigurationForm();
		//负责提交我的配置
		Form submitForm = configForm.createAnswerForm();
		//先获取默认的配置
		//遍历所有的默认配置
		for (FormField formField : configForm.getFields()) {
			//为空字符串，或者为NULL的默认配置项，不用读取出来去提交
			if (!formField.getType().equals(FormField.Type.hidden)
					&& formField.getVariable() != null
					) {
				submitForm.setDefaultAnswer(formField.getVariable());
			}
		}
		
		//一些额外的配置
		List<String> owners = new ArrayList<String>();
		owners.add(connection.getUser());
		submitForm.setAnswer("muc#roomconfig_roomowners", owners);
		submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
		// 设置聊天室是持久聊天室，即将要被保存下来
		submitForm.setAnswer("muc#roomconfig_persistentroom", true);
		// 房间仅对成员开放
		submitForm.setAnswer("muc#roomconfig_membersonly", false);
		// 允许占有者邀请其他人
		submitForm.setAnswer("muc#roomconfig_allowinvites", true);
		// 允许加入的成员数
		submitForm.setAnswer("muc#roomconfig_maxusers", Arrays.asList("30"));
		// 能够发现占有者真实 JID 的角色
		// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
		// 登录房间对话
		submitForm.setAnswer("muc#roomconfig_enablelogging", true);
		// 仅允许注册的昵称登录
		submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
		// 允许使用者修改昵称
		submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
		// 允许用户注册房间
		submitForm.setAnswer("x-muc#roomconfig_registration", false);
		// 进入是否需要密码  
        submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",true); 
		// 设置进入密码
		submitForm.setAnswer("muc#roomconfig_roomsecret", password);
		submitForm.setAnswer("muc#roomconfig_roomdesc", description);
		muc.sendConfigurationForm(submitForm);

		return muc;
	}
	
	
	
	/**
	 * 加入房间（群组）
	 * @param connection
	 * @param roomName 房间名称
	 * @param password 加入房间的密码
	 * @param nickname 用户在群里使用的别名
	 * @return 
	 */
	public static MultiUserChat joinRoom(AbstractXMPPConnection connection,String roomName,String password,String nickname) throws Exception{
		//群聊管理器
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		String jid = roomName + CONFERENCE + connection.getServiceName();
		MultiUserChat muc = manager.getMultiUserChat(jid);

		//获取加入房间之前的历史消息
		DiscussionHistory history = new DiscussionHistory();
		//从哪个日期开始的历史消息
		history.setSince(new Date(2015, 1, 1));
		
		//5秒超时
		muc.join(nickname,password,history,5000);
		//muc.nextMessage(); 拿到历史消息
		
		return muc;
		
	}
	
	/**
	 * 获取已经加入的的群组列表
	 * @param connection
	 * @return
	 */
	public static List<ChatRoom> getJoinedRooms(AbstractXMPPConnection connection){
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		//群聊管理器
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		//获取所有加入的房间的jid集合
		Set<String> rooms = manager.getJoinedRooms();
		for (String room : rooms) {
			try {
				RoomInfo info = manager.getRoomInfo(room);
				ChatRoom cr = new ChatRoom();
				//jid
				cr.setJid(info.getRoom());
				//名称
				cr.setName(info.getName());
				//描述
				cr.setDescription(info.getDescription());
				//人数
				cr.setOccupantsCount(info.getOccupantsCount());
				
				list.add(cr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return list;
	}
	
	
	/**
	 * 收藏一个群组
	 * @param connection
	 * @param name
	 * @param nickname
	 * @param password
	 */
	public static void addBookmarkedRoom(AbstractXMPPConnection connection, String name,String nickname,String password){
		try {
			BookmarkManager manager = BookmarkManager.getBookmarkManager(connection);
			String jid = name + ChatRoomManager.CONFERENCE + connection.getServiceName();
			manager.addBookmarkedConference(name, jid, true, nickname, password);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	/**
	 * 获取已经收藏了的所有群组
	 * @param connection
	 * @return
	 */
	public static List<ChatRoom> getBookmarkedRooms(AbstractXMPPConnection connection){
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		try {
			//群聊管理器
			MultiUserChatManager mucm = MultiUserChatManager.getInstanceFor(connection);
			BookmarkManager bm = BookmarkManager.getBookmarkManager(connection);
			List<BookmarkedConference> conferences = bm.getBookmarkedConferences();
			for (BookmarkedConference c : conferences) {
				ChatRoom room = new ChatRoom();
				room.setJid(c.getJid());
				room.setName(c.getName());
				room.setPassword(c.getPassword());
				
				//获取到之后立马join
				String nickname = XmppStringUtils.parseLocalpart(connection.getUser());
				joinRoom(connection, c.getName(), c.getPassword(), nickname);
				
				SystemClock.sleep(500);
				
				//描述
				RoomInfo info = mucm.getRoomInfo(c.getJid());
				room.setDescription(info.getDescription());
				//人数
				room.setOccupantsCount(info.getOccupantsCount());
				
				list.add(room);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 获得一个群组的对话
	 * @param connection
	 * @param jid 房间的jid
	 * @return
	 */
	public static MultiUserChat getMUC(AbstractXMPPConnection connection,String jid){
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		return manager.getMultiUserChat(jid);	
	}

	/**
	 * 群是否存在
	 * @param connection
	 * @param name 房间的名称
	 * @return
	 */
	public static boolean isChatRoom(AbstractXMPPConnection connection,String name){
		String jid = name + CONFERENCE + connection.getServiceName();
		MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
		try {
			List<HostedRoom> MultiUserChatManager=manager.getHostedRooms(manager.getServiceNames().get(0));
			if(MultiUserChatManager!=null){
				for (HostedRoom item: MultiUserChatManager){
					if(item.getJid().equals(jid)){
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
