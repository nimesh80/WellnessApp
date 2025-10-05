package com.example.wellnessapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Wellness Reminder"
        val isHydration = intent.getBooleanExtra("is_hydration", false)
        val reminderId = intent.getIntExtra("reminder_id", 0)

        // Check notification permission
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Show notification
        showNotification(context, title, reminderId)

        // Reschedule if hydration reminder
        if (isHydration) {
            rescheduleHydrationReminder(context, reminderId)
        }
    }

    private fun showNotification(context: Context, title: String, notificationId: Int) {
        val contentIntent = NotificationHelper.createNotificationIntent(context)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(getNotificationMessage(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun getNotificationMessage(title: String): String {
        return when {
            title.contains("Hydration", ignoreCase = true) -> "Time to drink water"
            title.contains("Habit", ignoreCase = true) -> "Complete your daily habit"
            else -> "Don't forget: $title"
        }
    }

    private fun rescheduleHydrationReminder(context: Context, reminderId: Int) {
        val prefs = SharedPrefsManager(context)
        val intervalMinutes = prefs.loadHydrationInterval()
        val isActive = prefs.loadHydrationStatus()

        if (!isActive) return

        val nextTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, intervalMinutes)
        }.timeInMillis

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Hydration Reminder")
            putExtra("is_hydration", true)
            putExtra("reminder_id", reminderId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTime,
            pendingIntent
        )
    }
}