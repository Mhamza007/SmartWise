package com.stackbuffers.groceryclient.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val ADMIN_CHANNEL_ID = "admin_channel"
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random().nextInt(3000)

        val prefs = SharedPreference(this)
        val notificationSenderId = prefs.getUserId()!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        val deviceIdSplit = remoteMessage.data["title"]!!.split("/")
        val senderId = deviceIdSplit[0]
        val senderName = deviceIdSplit[1]
        val receiverId = deviceIdSplit[2]
        val message = remoteMessage.data["message"]

        if (notificationSenderId != senderId) {
            val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.app_logo)
            val notificationSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(senderName)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.color = resources.getColor(R.color.colorPrimaryDark)
            }
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager.createNotificationChannel(adminChannel)
    }
}