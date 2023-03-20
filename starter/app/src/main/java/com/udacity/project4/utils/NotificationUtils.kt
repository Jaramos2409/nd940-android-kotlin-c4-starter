package com.udacity.project4.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.NotificationConstants.REMINDER_NOTIFICATION_CHANNEL_ID
import timber.log.Timber

object NotificationUtils {
    fun sendReminderNotification(context: Context, reminderDataItem: ReminderDataItem) {
        Timber.i("Inside of sendNotification")
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent =
            ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)

        //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
        val stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(ReminderDescriptionActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
            .getPendingIntent(
                getUniqueId(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

//    build the notification object with the data to be shown
        val notification = NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminderDataItem.title)
            .setContentText(reminderDataItem.location)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())
}

object NotificationConstants {
    const val REMINDER_NOTIFICATION_CHANNEL_ID =
        BuildConfig.APPLICATION_ID + ".channel.reminders"
}