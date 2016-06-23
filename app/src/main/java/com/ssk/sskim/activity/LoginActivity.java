package com.ssk.sskim.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ssk.sskim.R;
import com.ssk.sskim.beans.Appinfo;
import com.ssk.sskim.manager.ConnectionManager;
import com.ssk.sskim.service.MessageService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;

/**
 * 登录
 */
public class LoginActivity extends BaseToolBarActivity{

    private AppCompatEditText et_password,et_username;
    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        et_password= (AppCompatEditText) findViewById(R.id.et_password);
        et_username= (AppCompatEditText) findViewById(R.id.et_username);
    }
    class LoginTask extends AsyncTask<String, Void, Exception> {

        @Override
        protected Exception doInBackground(String... params) {
            //获取用户名密码
            String username =params[0];
            String password =params[1];
            ConnectionManager.openConnection();
            AbstractXMPPConnection connection;
                //登录
            try {
                connection = ConnectionManager.getConnection();
                connection.login(username, password);
                //登录成功，发送状态给服务器更新用户的在线状态
                Presence p = new Presence(Presence.Type.available);
                connection.sendStanza(p);
                Intent intent = new Intent();
                intent.setClass(getContext(), MessageService.class);
                getContext().startService(intent);//启动服务
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result==null) {
                Log.d("jason", "登录成功！");
                Appinfo.setUsername(et_username.getText().toString());
                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                //跳转到好友列表
                BaseActivity.start(getContext(),FriendsActivity.class);
            }else{
                if (result instanceof XMPPException.XMPPErrorException) {

                    XMPPException.XMPPErrorException xe = (XMPPException.XMPPErrorException)result;
                    switch ( xe.getXMPPError().getCondition() )
                    {
                        case conflict:
                            Toast.makeText(LoginActivity.this, "账号已登录，无法重复登录。", Toast.LENGTH_SHORT).show();
                            break;
                        case not_authorized:
                            Toast.makeText(LoginActivity.this, "错误的用户名或密码。", Toast.LENGTH_SHORT).show();
                            break;
                        case remote_server_not_found:
                            Toast.makeText(LoginActivity.this, "无法连接到服务器: 不可达的主机名或地址。", Toast.LENGTH_SHORT).show();
                            break;
                        case remote_server_timeout:
                            Toast.makeText(LoginActivity.this, "法连接到服务器: 不可达的主机名或地址。", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    public void doSubmit(View v){
        if(TextUtils.isEmpty(et_username.getText())){
            Toast.makeText(LoginActivity.this, "请输入账号！", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(et_password.getText())){
            Toast.makeText(LoginActivity.this, "请输入密码！", Toast.LENGTH_SHORT).show();
        }else{
            new LoginTask().execute(et_username.getText().toString(),et_password.getText().toString());
        }
    }
    @Override
    public boolean getBack() {
        return false;
    }
}

