package com.ssk.sskim.beans;

import java.util.List;

/**
 * 联系人分组
 * RosterGroup
 * @author Jason QQ: 2904048107
 * @date 2015年8月7日
 * @version 1.0
 */
public class GroupInfo {

	// 组名
	private String groupName;

	//所属的联系人列表
	private List<FriendInfo> friends;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<FriendInfo> getFriends() {
		return friends;
	}

	public void setFriends(List<FriendInfo> friends) {
		this.friends = friends;
	}

}
