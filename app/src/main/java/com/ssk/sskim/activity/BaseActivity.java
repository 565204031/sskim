package com.ssk.sskim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * Created by 杀死凯 on 2016/5/19.
 * 基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static void start(Context ct,Class clazz){
        Intent intent =new Intent(ct,clazz);
        ct.startActivity(intent);
    }
    public static void start(Context ct,Class<? extends AppCompatActivity> clazz, Bundle bul){
        Intent intent =new Intent(ct,clazz);
        if(bul==null){
            bul=new Bundle();
        }
        intent.putExtras(bul);
        ct.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //4.4~5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }
    protected Context getContext(){
        return this;
    }
}
