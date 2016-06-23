package com.ssk.sskim.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.util.XmppStringUtils;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.ssk.sskim.R;
import com.ssk.sskim.beans.Appinfo;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.manager.RosterManager;

/**
 *  搜索好友页面
 */
public class FriendsAddActivity extends Activity implements OnItemClickListener {

	private EditText et_search;
	private AbstractXMPPConnection connection = ConnectionManager.getConnection();
	private ListView lv_results;
	private ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_add);
		et_search = (EditText) findViewById(R.id.et_search);
		lv_results = (ListView) findViewById(R.id.lv_results);
		//点击搜索出来的联系人，发送添加好友的请求
		lv_results.setOnItemClickListener(this);
	}
	
	
	/**
	 * 点击搜索按钮
	 * @param btn
	 */
	public void search(View btn){
		new SearchTask().execute(et_search.getText().toString());
	}
	
	
	
	class SearchTask extends AsyncTask<String, Void, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			//获取输入的用户名
			String searchText =params[0];
			//搜索管理器
			UserSearchManager searchManager = new UserSearchManager(connection);
			String searchService = "search." + connection.getServiceName();
			try {
				Form searchForm = searchManager.getSearchForm(searchService);
				//设置搜索的条件
				Form answerForm = searchForm.createAnswerForm();
				//根据用户名进行搜索
				answerForm.setAnswer("Username", true);
				//指定搜索的用户名关键字
				answerForm.setAnswer("search", searchText.trim());
				
				//获取查询结构
				ReportedData results = searchManager.getSearchResults(answerForm, searchService);
				//遍历行
				List<Row> rows = results.getRows();
				List<String> list = new ArrayList<String>();
				for (Row row : rows) {
					String username = row.getValues("Username").get(0);
					if(!username.equals(Appinfo.getUsername())){
						list.add(username);
					}
				}
				adapter = new ArrayAdapter<String>(FriendsAddActivity.this, R.layout.friend_search, R.id.tv_name,list);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				lv_results.setAdapter(adapter);
			}
		}
		
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//点击搜索出来的联系人，发送添加好友的请求
		String name = adapter.getItem(position);
		//转为jid
		//rose@sina.com
		final String addToJid = XmppStringUtils.completeJidFrom(name, connection.getServiceName());
		//弹出提示对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("添加好友");
		builder.setMessage("确定添加"+name+"为好友吗？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RosterManager rosterManager = RosterManager.getInstance(connection);
				//添加的“我的好友”这组
				String nickname = XmppStringUtils.parseLocalpart(addToJid);
				rosterManager.addEntry(addToJid, nickname, "我的好友");
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	
}
