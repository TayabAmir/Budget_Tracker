package com.cs173.myapplication.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cs173.myapplication.AppData
import java.text.SimpleDateFormat
import java.util.*

class BillReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bill_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bill Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (intent.action == Intent.ACTION_DATE_CHANGED) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())
            
            AppData.subscriptions.filter { it.isActive && it.nextBillingDate == today }.forEach { sub ->
                showNotification(context, notificationManager, channelId, "Bill Due Today", "Bill due today: ${sub.name} — Rs. ${sub.amount}")
            }
        } else if (intent.action == "com.smartbudget.BILL_REMINDER") {
            val subName = intent.getStringExtra("subName") ?: "Subscription"
            val amount = intent.getDoubleExtra("amount", 0.0)
            showNotification(context, notificationManager, channelId, "Upcoming Bill", "$subName is due tomorrow — Rs. $amount")
        }
    }

    private fun showNotification(context: Context, manager: NotificationManager, channelId: String, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
