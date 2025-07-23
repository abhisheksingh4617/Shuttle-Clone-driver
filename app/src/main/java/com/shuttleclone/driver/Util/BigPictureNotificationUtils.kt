package com.shuttleclone.driver.Util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.shuttleclone.driver.ui.Activity.MainActivity
import com.shuttleclone.driver.R
import com.google.gson.Gson
import com.shuttleclone.driver.Model.InfoPopUpData


fun Context.showPopInfoNotification(
    channelId: String,
    title: String,
    body: String,
    infoPopUp: String,
    notificationId: Int,
): NotificationCompat.Builder? {

    var builder: NotificationCompat.Builder? = null
    var infoPopUpData: InfoPopUpData? = null
    val fullScreenIntent = PendingIntent.getActivity(
        this,
        1222,
        Intent(),
        PendingIntent.FLAG_IMMUTABLE
    )

    try {
        val bitmap = arrayOf<Bitmap?>(null)
        var imageUrl = ""

        try {
            infoPopUpData = Gson().fromJson(
                infoPopUp,
                InfoPopUpData::class.java
            )
            imageUrl = infoPopUpData?.imgurl.toString().trim()

            if (!imageUrl.equals("") && !imageUrl.equals("null") && !imageUrl.equals(null)) {
                Glide.with(this)
                    .asBitmap()
                    .load(infoPopUpData?.imgurl)
                    .into(object : CustomTarget<Bitmap?>() {

                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            bitmap[0] = resource
                            displayImageNotification(bitmap[0])
                        }

                        private fun displayImageNotification(bitmap: Bitmap?) {
                            val bigPictureStyle =
                                NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                                    .setSummaryText(body).setBigContentTitle(title)

                            builder =
                                NotificationCompat.Builder(this@showPopInfoNotification, channelId)
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setSmallIcon(R.drawable.ic_notification_logo)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true)
                                    .setColor(
                                        ContextCompat.getColor(
                                            this@showPopInfoNotification,
                                            R.color.colorAccent
                                        )
                                    )
                                    .setColorized(true)
                                    .setContentIntent(getFullScreenIntent(infoPopUp))
                                    .setLargeIcon(bitmap)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setStyle(bigPictureStyle)
                                    .setDefaults(Notification.DEFAULT_VIBRATE)
                                    .setFullScreenIntent(fullScreenIntent, true)

                            val notificationManager =
                                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            with(notificationManager) {
                                buildChannel(channelId)

                                val notification = builder?.build()

                                notify(notificationId, notification)
                            }

                        }
                    })

            } else {

                val bigTextStyle =
                    NotificationCompat.BigTextStyle().bigText(body)

                builder = NotificationCompat.Builder(this@showPopInfoNotification, channelId)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setColorized(true)
                    .setStyle(bigTextStyle)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.ic_app_logo
                        )
                    )
                    .setContentIntent(getFullScreenIntent(infoPopUp))
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setFullScreenIntent(fullScreenIntent, true)

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                with(notificationManager) {
                    buildChannel(channelId)

                    val notification = builder?.build()

                    notify(notificationId, notification)
                }


            }


        } catch (e: Exception) {
            myLog("TAG", "showPopInfoNotification: fError=${e.localizedMessage}")
        }


        return builder
    } catch (e: Exception) {
        myLog("TAG", "showBigPictureNotification: Error=${e.localizedMessage}")
        return null
    }

}

private fun NotificationManager.buildChannel(channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "BigPictureNotification"
        val descriptionText = "This is used for BigPictureNotification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = descriptionText
        channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

        createNotificationChannel(channel)
    }
}

private fun Context.getFullScreenIntent(infoPopUp: String): PendingIntent {
    var pendingIntent: PendingIntent? = null
    if (!getPreference(this, AppConstants.TOKEN).equals("")) {
        val intent = Intent(this, MainActivity::class.java)
        myLog("infoPopUp", "getFullScreenIntent: infoPopUpinfoPopUp =$infoPopUp")
        intent.putExtra("infoPopUp", infoPopUp)
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
