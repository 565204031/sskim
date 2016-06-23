package com.ssk.sskim.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ssk.sskim.activity.ChatActivity;
import com.ssk.sskim.activity.ChatRoomChatingActivity;
import com.ssk.sskim.beans.DbMessage;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.manager.ChatRoomManager;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.manager.RosterManager;
import com.ssk.sskim.utils.Constants;
import com.ssk.sskim.utils.DbUtils;
import com.ssk.sskim.utils.NotificationUtils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.util.XmppStringUtils;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import de.measite.minidns.record.A;

/**
 * Created by 杀死凯 on 2016/5/27.
 */
public class MessageService extends Service{


    //联系人管理器
    private RosterManager rosterManager;
    //联系人列表
    private Roster roster;
    //聊天管理器
    private ChatManager chatManager;
    //群聊管理器
    private MultiUserChat  muc;
    //状态管理器
    private ChatStateManager chatStateManager;
    //链接
    private AbstractXMPPConnection connection;
    //文件接收管理
    private FileTransferManager fileTransferManager;

    public static final String ACTION_MESSAGE = "com.ssk.sskim.action.message";

    //聊天消息
    public static final int TYPE_CHATMESSAGE=1;

    //聊天状态
    public static final int TYPE_CHATSTATE=2;

    //好友状态
    public static final int TYPE_PRESENC_ADD=3;

    //接收文件完成
    public static final int TYPE_FILE_FINISH=4;

    //群聊天消息
    public static final int TYPE_CHATROOMMESSAGE=5;

    private DbManager db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = x.getDb(DbUtils.daoConfig);
        //好友状态监听
        addStanzaListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("INFO", "IBinder: ");
        afreshconnection();
        return new MyBinder();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("INFO", "onStartCommand: ");
        connection = ConnectionManager.getConnection();
        return super.onStartCommand(intent, flags, startId);
    }
    public class MyBinder extends Binder {
        public MessageService getService(){
            return MessageService.this;
        }
    }

    //重新连接
    public void afreshconnection(){
        if(connection!=null){
            rosterManager = RosterManager.getInstance(connection);
            roster = Roster.getInstanceFor(connection);
            chatManager = ChatManager.getInstanceFor(connection);
            chatManager.addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    //设置会话监听
                    chat.addMessageListener(new myChatMessages());
                }
            });
            chatStateManager=ChatStateManager.getInstance(connection);
            fileTransferManager = FileTransferManager.getInstanceFor(connection);
            addFileTransferListener();
            addRommMessageListener();
        }
    }

    //房间信息
    private void addRommMessageListener(){
        BookmarkManager bm = null;
        try {
            bm = BookmarkManager.getBookmarkManager(connection);
            List<BookmarkedConference> conferences = bm.getBookmarkedConferences();
            for (BookmarkedConference c : conferences) {
                //获取一个群组（多人）的对话
                muc = ChatRoomManager.getMUC(connection,  c.getJid());
                muc.addMessageListener(new MessageListener(){

                    @Override
                    public void processMessage(Message message) {
                        if(!TextUtils.isEmpty(message.getBody())){
                            String fromJid = XmppStringUtils.parseBareJid(message.getFrom());
                            addDb(fromJid,message.getBody(),false);
                            String content="";
                            IMessage info=IMessage.toParse(message.getBody());
                            content=info.getContent();
                            NotificationUtils.showNotification(MessageService.this.getBaseContext(),ChatRoomChatingActivity.class,fromJid,content);
                            doMessageBroadcast(TYPE_CHATMESSAGE,message.getBody(),fromJid);
                            getdb();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //传输文件监听
    private void addFileTransferListener(){
        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                //文件名
                String fileName=request.getFileName();
                if(TextUtils.isEmpty(fileName)){
                    return;
                }
                IncomingFileTransfer  transfer=null;
                //后缀名，转小写
                String suffixName=fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
                File localdir;
                if(suffixName.equals("amr")){
                    //音频 ，自动接收
                    transfer =  request.accept();
                    if (!Constants.AUDIO_DIR.exists()) {
                        Constants.AUDIO_DIR.mkdirs();
                    }
                    localdir= Constants.AUDIO_DIR;
                }else if(suffixName.equals("jpg")||suffixName.equals("png")){
                    //图片 ，自动接收
                   transfer =  request.accept();
                    if (!Constants.IMG_DIR.exists()) {
                        Constants.IMG_DIR.mkdirs();
                    }
                    localdir= Constants.IMG_DIR;
                }else{
                    //其他文件手动接收
                    transfer =  request.accept();
                    return;
                }
                File file = new File(localdir,transfer.getFileName());
                try {
                    transfer.recieveFile(file);
                    //传输成功，更新消息的状态
                    new Thread(new FileRunnable(transfer)).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //监听服务器转发的消息（请求加好友的消息）
    private void addStanzaListener() {
        StanzaFilter stanzaFilter = new StanzaTypeFilter(Presence.class);
        ConnectionManager.getConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Log.i("INFO","addAsyncStanzaListener");
                if (packet instanceof Presence) {
                    Presence p = (Presence) packet;
                    //获取请求者
                    String inverstorJid = packet.getFrom();
                    if (p.getType() == Presence.Type.subscribe) {
                        //如果已经addEntry了，不用再提示
                        RosterEntry entry = roster.getEntry(inverstorJid);
                        if(entry==null){
                            doMessageBroadcast(TYPE_PRESENC_ADD,"",inverstorJid);
                        }
                    }
                }
            }
        }, stanzaFilter);
    }

    public ChatManager getChatManager(){
          return chatManager;
    }
    public RosterManager getRosterManager(){return rosterManager;}
    public Roster getRoster(){return roster;};
    public AbstractXMPPConnection getConnection(){return connection;}
    public FileTransferManager getFileTransferManager(){return  fileTransferManager;}


    /**
     * 好友聊天消息
     */
    private class myChatMessages implements ChatStateListener, MessageListener{

        private Message message;
        @Override
        public void stateChanged(Chat chat, ChatState state) {
            //active（参加会话）, composing（正在输入）, gone（离开）, inactive（没有参加会话）, paused（暂停输入）。
            String fromJid = XmppStringUtils.parseBareJid(message.getFrom());
            doMessageBroadcast(TYPE_CHATSTATE,state==ChatState.composing?"composing":"",fromJid);
        }

        @Override
        public void processMessage(Chat chat, Message message) {
            this.message=message;
            if(!TextUtils.isEmpty(message.getBody())){
                String fromJid = XmppStringUtils.parseBareJid(message.getFrom());
                addDb(fromJid,message.getBody(),false);
                String content="";
                IMessage info=IMessage.toParse(message.getBody());
                if(info.getMsgModle()==IMessage.MESSAGE_MODEL_TEXT){
                    content=info.getContent();
                }else if(info.getMsgModle()==IMessage.MESSAGE_MODEL_IMG){
                    content="[图片]";
                }else{
                    content="[语音]";
                }
                NotificationUtils.showNotification(MessageService.this.getBaseContext(),ChatActivity.class,fromJid,content);
                doMessageBroadcast(TYPE_CHATMESSAGE,message.getBody(),fromJid);
                getdb();
            }
        }
        @Override
        public void processMessage(Message message) {
            Log.i("INFO","processMessage");
        }
    }

    /**
     * 保存消息
     * @param jid
     * @param json
     */
    public void addDb(String jid,String json,boolean read){
        DbMessage info=new DbMessage();
        String loggedUser = XmppStringUtils.parseBareJid(connection.getUser());
        info.setUid(loggedUser);
        info.setJid(jid);
        info.setContent(json);
        info.setRead(read);
        try {
            db.saveOrUpdate(info);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    private void doMessageBroadcast(int type,String data,String jid){
        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra("type",type);
        intent.putExtra("data",data);
        intent.putExtra("jid",jid);
        sendBroadcast(intent);
    }
    public void getdb(){
        try {
            List<DbMessage> list= db.selector(DbMessage.class).findAll();
            if(list!=null){
               for (DbMessage item:list){
                   Log.i("INFO",item.isRead()+"---");
               }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private class  FileRunnable implements Runnable{

        private IncomingFileTransfer transfer;
        public FileRunnable(IncomingFileTransfer transfer){
            this.transfer=transfer;
            if(transfer==null){
                return;
            }
        }
        @Override
        public void run() {
            while (true){
                if(transfer.getStatus()== FileTransfer.Status.complete){
                    doMessageBroadcast(TYPE_FILE_FINISH,transfer.getFileName(),"");
                }
            }
        }
    }


}
