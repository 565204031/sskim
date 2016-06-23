package com.ssk.sskim.activity;

import android.os.Bundle;
import android.widget.ListView;

import com.ssk.sskim.R;
import com.ssk.sskim.adapter.ChatListAdapter;
import com.ssk.sskim.beans.DbMessage;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.utils.DbUtils;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 杀死凯 on 2016/5/31.
 * 历史记录
 */
public class ChatHistoryActivity extends BaseToolBarActivity{
    private String chattoJid,uid;
    private List<IMessage> msgList = new ArrayList<IMessage>();
    private ChatListAdapter adapter;

    private DbManager db;
    @Override
    protected int getContentView() {
        return R.layout.chat_listview;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        db = x.getDb(DbUtils.daoConfig);
        //当前的聊天对象
        chattoJid = getIntent().getStringExtra("chattoJid");
        uid = getIntent().getStringExtra("uid");
        ListView lv_msg = (ListView) findViewById(R.id.lv_msg);
        adapter = new ChatListAdapter(this, msgList);
        lv_msg.setAdapter(adapter);
        initData();
    }
    private void initData() {
        try {
            List<DbMessage> list;
            list=db.selector(DbMessage.class).where(WhereBuilder.b("jid","==",chattoJid).and("uid","==",uid)).findAll();
            if(list!=null){
                for (DbMessage item:list){
                    msgList.add(IMessage.toParse(item.getContent()));
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}
