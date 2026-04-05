package com.blockick.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.blockick.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val UPDATE_CHANNEL_ID = "profile_updates_channel"
        const val UPDATE_NOTIFICATION_ID = 101
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_updates_name)
            val descriptionText = context.getString(R.string.notif_channel_updates_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(UPDATE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showUpdateStarted() {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_purple)
            .setContentTitle(context.getString(R.string.notif_update_start_title))
            .setContentText(context.getString(R.string.notif_update_start_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(UPDATE_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Silently fail if permission is missing
            }
        }
    }

    fun showUpdateComplete() {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_purple)
            .setContentTitle(context.getString(R.string.notif_update_complete_title))
            .setContentText(context.getString(R.string.notif_update_complete_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(UPDATE_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Silently fail
            }
        }
    }
    
    fun cancelUpdateNotification() {
        with(NotificationManagerCompat.from(context)) {
            cancel(UPDATE_NOTIFICATION_ID)
        }
    }
}
