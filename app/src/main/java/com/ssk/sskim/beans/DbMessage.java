package com.ssk.sskim.beans;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by 杀死凯 on 2016/5/29.
 * 聊天记录，群聊记录储存
 */
@Table(name = "message")
public class DbMessage {

    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "read")
    private boolean read;

    @Column(name = "jid")
    private String jid;

    @Column(name = "content")
    private String content;

    @Column(name = "uid")
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
