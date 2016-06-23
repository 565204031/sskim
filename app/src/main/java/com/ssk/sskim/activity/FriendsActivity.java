package com.ssk.sskim.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ssk.sskim.R;
import com.ssk.sskim.adapter.FriendsExpandableListAdapter;
import com.ssk.sskim.beans.FriendInfo;
import com.ssk.sskim.beans.GroupInfo;
import com.ssk.sskim.beans.IMessage;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.manager.RosterManager;
import com.ssk.sskim.manager.SubscribeManager;
import com.ssk.sskim.service.MessageService;
import com.ssk.sskim.utils.ImgUtils;
import com.ssk.sskim.utils.PictureUploading;
import com.ssk.sskim.utils.TimeUtils;
import com.ssk.sskim.utils.UUIDUtils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.util.XmppStringUtils;

import java.io.File;
import java.util.Collection;

/**
 * Created by 杀死凯 on 2016/5/27.
 * 好友列表
 */
public class FriendsActivity extends BindActivity implements ExpandableListView.OnChildClickListener, RosterListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private AbstractXMPPConnection connection;
    private RosterManager rosterManager;
    private Roster roster;
    private FriendsExpandableListAdapter adapter;

	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;
	private VCardManager mVCardManager;
	private ImageView iv_img;
	private TextView tv_name;
	private VCard vc;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(mMessageService!=null){
				mMessageService.afreshconnection();
				roster=mMessageService.getRoster();
				roster.addRosterListener(FriendsActivity.this);

			}
			adapter.setData(connection,roster,roster.getGroups(),roster.getEntries());
			adapter.notifyDataSetChanged();
		}
	};

    @Override
    protected int getContentView() {
        return R.layout.activity_friends;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
		mDrawerLayout =(DrawerLayout)findViewById(R.id.drawlayout);
		mNavigationView= (NavigationView)findViewById(R.id.navigationView);
		mNavigationView.setNavigationItemSelectedListener(this);
		iv_img= (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.iv_img);
		tv_name= (TextView)  mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
		iv_img.setOnClickListener(this);
		connection = ConnectionManager.getConnection();
		//显示当前登录用户的名称
		String loggedUser = connection.getUser();
		tv_name.setText(XmppStringUtils.parseBareJid(loggedUser));

		//官方提供的可旋转的导航菜单控件
		ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,R.string.draw_open,R.string.draw_close);
		//同步状态(刷新界面)
		drawerToggle.syncState();
		//给Drawlayout设置监听，1）自己实现监听   2)drawerToggle 自带了监听，会监听侧滑事件，同时对导航菜单设置了 旋转的效果
		mDrawerLayout.setDrawerListener(drawerToggle);

		//获取联系人列表
		roster = Roster.getInstanceFor(connection);
		//将联系人显示出来
		ExpandableListView elv = (ExpandableListView) findViewById(R.id.elv);
		adapter = new FriendsExpandableListAdapter(getContext(),roster.getGroups(),roster.getUnfiledEntries());
		elv.setAdapter(adapter);
		elv.setOnChildClickListener(this);
		rosterManager = RosterManager.getInstance(connection);
		mVCardManager=VCardManager.getInstanceFor(connection);
		try {
			 vc=mVCardManager.loadVCard(XmppStringUtils.parseBareJid(loggedUser));
			Bitmap bitmap=ImgUtils.Bytes2Bimap(vc.getAvatar());
			if(bitmap!=null){
				iv_img.setImageBitmap(bitmap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		onRefresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.friend_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		//获取被选中好友的jid
		FriendInfo info = (FriendInfo) adapter.getChild(groupPosition, childPosition);
		String chattoJid = info.getJid();
		GroupInfo group = (GroupInfo) adapter.getGroup(groupPosition);
		if(group.getGroupName().equals("陌生人")){
			alertInvestDialog2(chattoJid);
		}else{
			//跳转到聊天界面
			Intent intent = new Intent(this,ChatActivity.class);
			intent.putExtra("chattoJid", chattoJid);
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		onRefresh();
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		onRefresh();
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		onRefresh();
	}
	@Override
	public void presenceChanged(Presence presence) {
		String fromJid = XmppStringUtils.parseBareJid(presence.getFrom());
		FriendInfo friend =adapter.getItemByJid(fromJid);
		if(presence.getType()== Presence.Type.unavailable){
			friend.setMood("离线");
		}else{
			friend.setMood("在线");
		}
		onRefresh();
	}

	@Override
	public boolean getBack() {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				//跳转到添加好友界面
				BaseActivity.start(getContext(),FriendsAddActivity.class);
				break;
			case R.id.room:
				//操作群组页面
				BaseActivity.start(getContext(),ChatRoomOperateActivity.class);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onBindFinish(MessageService service) {
		//service.setMessageListener();
	}

	private void onRefresh(){
		handler.sendEmptyMessage(1);
	}
	protected void alertInvestDialog2(final String inverstorJid) {
		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
		builder.setTitle("提示");
		builder.setMessage("是否添加对方为好友？");
		builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//添加到“我的好友”组
				String nickname = XmppStringUtils.parseLocalpart(inverstorJid);
				mMessageService.getRosterManager().addEntry(inverstorJid, nickname, "我的好友");
				//通知请求的好友，你的请求已经通过了
				SubscribeManager.subscribed(mMessageService.getConnection(),inverstorJid);

			}
		});
		builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//请求不通过
				Roster roster  =mMessageService.getRoster();
				RosterEntry entry = roster.getEntry(inverstorJid);
				try {
					roster.removeEntry(entry);
					SubscribeManager.unsubscribe(mMessageService.getConnection(),inverstorJid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		android.support.v7.app.AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.logout){
			finish();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.iv_img:
				PictureUploading.showSelect(this, 0);
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PictureUploading.onActivityResult(this, new PictureUploading.OnBackCall() {

			@Override
			public void onSucceed(Bitmap bmp, int index) {
				//收到图片成功
				if(bmp!=null){
					vc.setAvatar(ImgUtils.Bitmap2Bytes(bmp));
					iv_img.setImageBitmap(bmp);
					try {
						vc.save(connection);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			@Override
			public void onFail() {
				Toast.makeText(getContext(),"图片参数错误",Toast.LENGTH_SHORT).show();
			}
		}, requestCode, resultCode, data);
	}
}
