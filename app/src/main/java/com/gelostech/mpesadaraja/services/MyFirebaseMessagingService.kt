package com.gelostech.mpesadaraja.services

import android.app.Notification
import android.content.Context
import android.content.Intent
import com.gelostech.mpesadaraja.utils.NotificationUtils
import org.json.JSONException
import android.text.TextUtils
import com.gelostech.mpesadaraja.activities.MainActivity
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.gelostech.mpesadaraja.commoners.Config
import org.json.JSONObject
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var notificationUtils: NotificationUtils? = null

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.e(TAG, "From: " + remoteMessage!!.from!!)

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(TAG, "Notification: " + remoteMessage.notification!!.toString())
            handleNotification(remoteMessage.notification)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())

            try {
                val json = JSONObject(remoteMessage.data.toString())
                handleDataMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }

        }
    }

    private fun handleNotification(notification: RemoteMessage.Notification?) {
        if (!NotificationUtils(this).isAppIsInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent(Config.PUSH_NOTIFICATION)
            pushNotification.putExtra("title", notification!!.title)
            pushNotification.putExtra("message", notification.body)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

            // play notification sound
            val notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    private fun handleDataMessage(json: JSONObject) {
        Log.e(TAG, "push json: " + json.toString())

        try {

            if (json.getString("type") == "topup"){
                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                pushNotification.putExtra("type", "topup")
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

                return

            } else if (json.getString("type") == "welcome") {

                val title = json.getString("title")
                val message = json.getString("message")
                val imageUrl = json.getString("image")
                val timestamp = json.getString("timestamp")

                val resultIntent = Intent(applicationContext, MainActivity::class.java)
                    resultIntent.putExtra("message", message)

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
                }
            } else if (json.getString("type") == "balance_update") {

                val title = json.getString("title")
                val message = json.getString("message")
                val imageUrl = json.getString("image")
                val timestamp = json.getString("timestamp")

                val resultIntent = Intent(applicationContext, MainActivity::class.java)
                resultIntent.putExtra("message", message)

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
                }
            }

        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }

    }

    /**
     * Showing notification with text only
     */
    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String, intent: Intent) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent)
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent, imageUrl)
    }


}