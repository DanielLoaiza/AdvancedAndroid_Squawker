package android.example.com.squawker.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.example.com.squawker.MainActivity
import android.example.com.squawker.R
import android.example.com.squawker.provider.SquawkContract.*
import android.example.com.squawker.provider.SquawkProvider
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.os.Build


const val NOTIFICATION_MAX_CHARACTERS = 30

class SquawkerMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val values = ContentValues()
        values.put(COLUMN_AUTHOR_KEY, data[COLUMN_AUTHOR_KEY])
        values.put(COLUMN_AUTHOR, data[COLUMN_AUTHOR])
        values.put(COLUMN_MESSAGE, data[COLUMN_MESSAGE])
        values.put(COLUMN_DATE, data[COLUMN_DATE])
        contentResolver.insert(SquawkProvider.SquawkMessages.CONTENT_URI, values)
        sendNotification(data[COLUMN_AUTHOR_KEY]!!, data[COLUMN_MESSAGE]!!)
    }

    private fun sendNotification(author: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        createChannel()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Create the pending intent to launch the activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        var sendMessage = message
        if (sendMessage.length > NOTIFICATION_MAX_CHARACTERS) {
            sendMessage = message.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026"
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "DefaultChannel")
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(sendMessage)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "DefaultChannel"
            val description = "DefaultDescripttion"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(name, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}