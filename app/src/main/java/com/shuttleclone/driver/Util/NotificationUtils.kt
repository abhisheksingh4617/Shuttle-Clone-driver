
package com.shuttleclone.driver.Util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shuttleclone.driver.ui.Activity.MainActivity
import com.shuttleclone.driver.R


fun Context.showAlertNotification(
    channelId: String,
    title: String,
    body: String
): NotificationCompat.Builder? {

    val fullScreenIntent = PendingIntent.getActivity(
        this,
        1222,
        Intent(),
        PendingIntent.FLAG_IMMUTABLE
    )
    val bigTextStyle =
        NotificationCompat.BigTextStyle().bigText(body)

    val builder = NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(R.drawable.ic_notification_logo)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setColorized(true)
        .setAutoCancel(true)
        .setContentIntent(getIntent())
        .setStyle(bigTextStyle)
        .setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_app_logo
            )
        )
        .setDefaults(Notification.DEFAULT_VIBRATE)
        .setFullScreenIntent(fullScreenIntent, true)



    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    with(notificationManager) {
        buildChannel(channelId)

        val notification = builder.build()

        notify(102, notification)
    }


    return builder
}

private fun NotificationManager.buildChannel(channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "IncomingNotification"
        val descriptionText = "This is used for Full Screen Intent"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = descriptionText
                channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

        createNotificationChannel(channel)
    }
}

private fun Context.getIntent(): PendingIntent {
    var pendingIntent: PendingIntent? = null
    if (!getPreference(this, AppConstants.TOKEN).equals("")) {
        val intent = Intent(this, MainActivity::class.java)

        intent.flags = (Intent.FLAG_ACTIVITY_SINGLE_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_NEW_TASK)
        pendingIntent = PendingIntent.getActivity(
            this,
            1221,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    } else {
        pendingIntent = PendingIntent.getActivity(
            this,
            1221,
            Intent(),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    return pendingIntent!!
}