package com.narcic.doh.net

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.narcic.doh.MainActivity
import com.narcic.doh.R
import com.narcic.doh.Strings.app_name
import com.narcic.doh.Strings.channel_id
import com.narcic.doh.Strings.package_name

object ServiceNotification {
    fun build(ctx: Context, text: String): Notification {
        val channel = NotificationChannel(channel_id, app_name, NotificationManager.IMPORTANCE_DEFAULT)
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
        val intent = ctx.packageManager.getLaunchIntentForPackage(package_name)
        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(ctx, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                FLAG_IMMUTABLE or FLAG_NO_CREATE or FLAG_UPDATE_CURRENT or FLAG_CANCEL_CURRENT
            )
        }
        return NotificationCompat.Builder(ctx, channel_id)
            .setContentTitle(app_name)
            .setSmallIcon(R.drawable.notification_logo)
            .setLargeIcon(BitmapFactory.decodeResource(ctx.resources, R.drawable.notification_logo))
            .setContentText(text)
            .setContentIntent(pendingIntent).build()
    }
}
