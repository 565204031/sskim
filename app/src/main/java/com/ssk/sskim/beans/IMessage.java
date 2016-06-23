package com.ssk.sskim.beans;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * 对org.jivesoftware.smack.packet.Message类的一个扩展
 * 
 * @author Jason QQ: 2904048107
 * @date 2015年8月12日
 * @version 1.0
 */

/**
 * 对org.jivesoftware.smack.packet.Message类的一个扩展
 * 杀死凯
 */
public class IMessage {

	// 消息的发送者
	private String sender;
	// 消息的内容
	private String content;
	// 消息的日期
	private String date;
	// 消息的类型，发出的消息OUT，收到的消息IN
	private int msgType;

	public static final int MESSAGE_TYPE_IN = 1;
	public static final int MESSAGE_TYPE_OUT = 2;

	// 消息的模式，文本、录音、图片等
	private int msgModle;

	public static final int MESSAGE_MODEL_TEXT = 1;
	public static final int MESSAGE_MODEL_AUDIO = 2;
	public static final int MESSAGE_MODEL_IMG = 3;

	// 语音消息的时长
	private int duration;

	// 文件的名称
	private String fileName;

	// 消息的状态，成功、失败、等待
	private int status;

	public static int MESSAGE_STATUS_SUCCESS = 1;
	public static int MESSAGE_STATUS_FAIL = 2;
	public static int MESSAGE_STATUS_WAIT = 3;

	public IMessage() {
	}

	/**
	 * 此构造方法用于构造文本消息
	 * 
	 * @param sender
	 * @param content
	 * @param date
	 * @param msgType
	 */
	public IMessage(String sender, String content, String date, int msgType) {
		super();
		this.sender = sender;
		this.content = content;
		this.date = date;
		this.msgType = msgType;
		this.msgModle=MESSAGE_MODEL_TEXT;
	}


	/**
	 * 此构造方法用户构造语音消息
	 * 
	 * @param sender
	 * @param date
	 * @param msgType
	 * @param duration
	 *            语音的时长
	 * @param fileName
	 *            语音文件的名称
	 */
	public IMessage(String sender, String date, int msgType, int duration, String fileName) {
		super();
		this.sender = sender;
		this.date = date;
		this.msgType = msgType;
		this.duration = duration;
		this.fileName = fileName;
		this.content = (duration / 1000) + "\'" + (duration % 1000) + "\"语音消息";
		this.msgModle = MESSAGE_MODEL_AUDIO;
		this.status=MESSAGE_STATUS_WAIT;

	}
	/**
	 * 此构造方法用户构造图片消息
	 *
	 * @param sender
	 * @param date
	 * @param msgType
	 *            语音的时长
	 * @param fileName
	 *            语音文件的名称
	 */
	public IMessage(String sender, String date, int msgType, String fileName) {
		super();
		this.sender = sender;
		this.date = date;
		this.msgType = msgType;
		this.fileName = fileName;
		this.content = "图片";
		this.msgModle = MESSAGE_MODEL_IMG;
		this.status=MESSAGE_STATUS_WAIT;
	}

	/**
	 * 将IMessage对象转为json文本，便于作为文本消息进行发送
	 * 
	 * @return
	 */
	public String toJson() {
		return JSON.toJSONString(this);
	}

	/**
	 * Json转对象
	 * @param json
	 * @return
     */
	public static IMessage toParse(String json){
		return JSON.parseObject(json, IMessage.class);
	}

	/**
	 * 将json文本转为IMessage对象
	 * 
	 * @param json
	 * @return
	 */
	public static IMessage fromJson(String json) {
		return JSON.parseObject(json, IMessage.class);
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getMsgModle() {
		return msgModle;
	}

	public void setMsgModle(int msgModle) {
		this.msgModle = msgModle;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
