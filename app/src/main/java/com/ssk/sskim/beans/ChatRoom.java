package com.ssk.sskim.beans;

import java.io.Serializable;

/**
 * 群组信息
 * 
 * @author Jason QQ: 2904048107
 * @date 2015年8月17日
 * @version 1.0
 */
public class ChatRoom implements Serializable{

	private String jid;
	private String name;
	private String password;
	// 群人数
	private int occupantsCount;
	// 描述
	private String description;

	public ChatRoom() {

	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getOccupantsCount() {
		return occupantsCount;
	}

	public void setOccupantsCount(int occupantsCount) {
		this.occupantsCount = occupantsCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
