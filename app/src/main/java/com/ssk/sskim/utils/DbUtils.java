package com.ssk.sskim.utils;

import org.xutils.DbManager;

import java.io.File;

/**
 * Created by Administrator on 2016/4/27.
 */
public class DbUtils {

    public static  DbManager.DaoConfig daoConfig;
    public static void initDb(){
                 daoConfig = new DbManager.DaoConfig()
                .setDbName("sskim.db")
                        // 不设置dbDir时, 默认存储在app的私有目录.
                .setDbDir(new File("/sdcard")) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
                .setDbVersion(1)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                    }
                });

    }

}
