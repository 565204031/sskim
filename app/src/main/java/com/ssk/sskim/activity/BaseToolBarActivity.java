package com.ssk.sskim.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.ssk.sskim.R;

/**
 * Created by 杀死凯 on 2016/5/19.
 */
public abstract class BaseToolBarActivity extends BaseActivity {

    protected Toolbar mToolbar;
    private LinearLayout ll_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar);
        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        ll_content= (LinearLayout) findViewById(R.id.ll_content);
        mToolbar.inflateMenu(R.menu.friend_menu);
        setSupportActionBar(mToolbar);

        //设置返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(getBack());
        View contentView =LayoutInflater.from(this).inflate(getContentView(),null);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        contentView.setLayoutParams(lp);
        ll_content.addView(contentView);

        //api 19~21处理方式(4.4~5.0)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //解决状态栏 问题
            LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) mToolbar.getLayoutParams();
            //1.设置toolbar搞得+状态栏的高度
            lp2.height = lp2.height + getStatusBarHeight(this);
            mToolbar.setLayoutParams(lp2);
            //3,设置toolbar的padding
            mToolbar.setPadding(
                    mToolbar.getPaddingLeft(),
                    mToolbar.getPaddingTop() + getStatusBarHeight(this),
                    mToolbar.getPaddingRight(), mToolbar.getPaddingBottom());
        }
        initView(savedInstanceState);
    }
    protected abstract int getContentView();
    protected abstract void initView(Bundle savedInstanceState);

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //获取系统的Demin类的指定属性的值
    private static int getSystemDimne(Context context, String dimenName) {
        /**
         * java文件----编译.class
         */
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object objct = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField(dimenName).get(objct).toString());//api 23 height =24
            //height:dip---->像素：px
            statusHeight =  context.getResources().getDimensionPixelSize(height);

        }catch (Exception e){
            e.printStackTrace();
        }
        return statusHeight;
    }
    public static int getStatusBarHeight(Context context){
        return getSystemDimne(context,"status_bar_height");
    }
    public static int getNavigationBarHeight(Context context){
        return getSystemDimne(context,"navigation_bar_height");
    }
    public boolean getBack(){
        return true;
    }

    public void setTitle(String txt){
        mToolbar.setTitle(txt);
        setSupportActionBar(mToolbar);
    }
}
