package com.shuttleclone.driver.firebaseService

import android.media.RingtoneManager
import com.shuttleclone.driver.Util.myLog
import com.google.firebase.messaging.FirebaseMessagingService

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.showAlertNotification
import com.shuttleclone.driver.Util.showPopInfoNotification

import java.lang.Exception

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var title = ""
    private var body = ""
    private var infoPopUp: String? = ""
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            myLog(TAG, " data=" + remoteMessage.data)
            myLog(TAG, " title=" + remoteMessage.data["title"])
            myLog(TAG, " message=" + remoteMessage.data["message"])
            myLog(TAG, " step=" + remoteMessage.data["step"])
            myLog(TAG, " booking=" + remoteMessage.data["booking"])
            myLog(TAG, " Notification=" + remoteMessage.notification)
        } catch (e: Exception) {
            myLog(TAG, " Error=" + e.localizedMessage)
        }
        if (remoteMessage != null) {
            showNotification(remoteMessage)
        }
    }
    
    private fun showBookingNotification(title: String, message: String, notificationId: Int) {
        try {
            showPopInfoNotification(
                CHANNEL_ID,
                title,
                message,
                "",
                notificationId
            )
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            myLog(TAG, "showBookingNotification: Error=${e.localizedMessage}")
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        try {
            if (remoteMessage.notification != null) {
                title = remoteMessage.notification!!.title.toString()
                body = remoteMessage.notification!!.body.toString()
            } else {
                title = remoteMessage.data["title"].toString()
                body = remoteMessage.data["message"].toString()
                infoPopUp = remoteMessage.data["info_popup"]
                myLog(TAG, "showNotification: infoPopUp=$infoPopUp")
            }
            if (infoPopUp != null) this.showPopInfoNotification(
                CHANNEL_ID,
                title,
                body,
                infoPopUp!!,
                2121
            ) else this.showAlertNotification(
                CHANNEL_ID, title, body
            )
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            myLog(TAG, "showNotification: Err0r=" + e.localizedMessage)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        myLog("NEW_TOKEN = = == = = =", token)
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO)
    }

    companion object {
        private  val CHANNEL_ID = AppConstants.CHANNEL_NAME
        private const val CHANNEL_NAME = "Shuttle"
        private const val TAG = "MainFirebaseMessaging"
        private const val SUBSCRIBE_TO = "Driver"
    }
}