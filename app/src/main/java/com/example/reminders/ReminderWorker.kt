package com.example.reminders

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.google.android.gms.location.*


class ReminderWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters)  {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    override fun doWork(): Result {
        val reminderMessage = inputData.getString("reminderMessage")

        showNotification(applicationContext, reminderMessage)

        return Result.success()
    }

    fun showNotification(context: Context, reminderMessage: String?) {

        val channelId = "reminders_notifications3"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val soundUri = Uri.parse("android.resource://" + context.packageName + "/raw/remindernotif")
        val channel = NotificationChannel(channelId, "remindersApp", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, audioAttributes)
        }

        notificationManager.createNotificationChannel(channel)
        val notificationId = 1
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(reminderMessage)
            .setContentText("Reminder due")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }

    }
}