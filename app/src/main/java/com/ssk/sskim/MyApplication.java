package com.ssk.sskim;

import android.app.Application;

import com.ssk.sskim.utils.DbUtils;


/**
 * Created by 杀死凯 on 2016/5/29.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DbUtils.initDb();
    }
}
