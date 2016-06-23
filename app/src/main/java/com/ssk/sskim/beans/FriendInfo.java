package com.ssk.sskim.beans;

import android.graphics.Bitmap;

import com.ssk.sskim.manager.ConnectionManager;

import org.jivesoftware.smack.packet.Presence;

import java.lang.ref.SoftReference;


/**
 * 联系人信息
 */
public class FriendInfo {

	// 用户名
	private String username;
	// 名称
	private String name;
	// 心情
	private String mood;

	private Presence.Type type;

	private String jid;

	private SoftReference<Bitmap> photo;

	public SoftReference<Bitmap> getPhoto() {
		return photo;
	}

	public void setPhoto(SoftReference<Bitmap> photo) {
		this.photo = photo;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Presence.Type getType() {
		return type;
	}

	public void setType(Presence.Type type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

}
