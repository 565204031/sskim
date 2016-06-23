package com.ssk.sskim.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 杀死凯 on 2016/5/31.
 */
public class ImgUtils {
    /**
     * 保存文件
     * @param name
     * @param bitmap
     * @return
     */
    public static File save(String name, Bitmap bitmap){
        //输出文件的路径
        if (!Constants.IMG_DIR.exists()) {
            Constants.IMG_DIR.mkdirs();
        }
        File f = new File(Constants.IMG_DIR+"/"+name+".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    // Bytes 转Bimap
    public static Bitmap Bytes2Bimap(byte[] b) {
         if (b.length != 0) {
                 return BitmapFactory.decodeByteArray(b, 0, b.length);
             } else {
                 return null;
             }
     }
    // Bimap转 Bytes
    public static byte[] Bitmap2Bytes(Bitmap bm) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
         return baos.toByteArray();
     }
}
