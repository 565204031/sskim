package com.ssk.sskim.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import com.ssk.sskim.R;

/**
 * Created by 杀死凯 on 2016/6/1.
 */
public class NotificationUtils {

    public final static int NOTIFICATION_ID = "NotificationUtils".hashCode();

    public static void showNotification(Context ct, Class<?> clazz, String jid,String content){
        final NotificationManager nm = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
        int smallIconId = R.drawable.ic_launcher;
        Bitmap largeIcon = ((BitmapDrawable)ct.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap();

        Intent intent = new Intent(ct,clazz);
        intent.putExtra("chattoJid",jid);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ct);
        builder.setSmallIcon(smallIconId)
                .setContentTitle("你有一条新的消息")
                .setContentText(content)
                .setTicker(content)
                .setContentIntent(PendingIntent.getActivity(ct, 0, intent, 0));
        final Notification n = builder.build();
        n.flags=Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, n);
    }
}
