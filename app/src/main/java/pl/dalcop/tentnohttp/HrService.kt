package pl.dalcop.tentnohttp

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import pl.dalcop.tentnohttp.HttpServer.Companion.isRunning
import pl.dalcop.tentnohttp.HttpServer.Companion.stopHTTP

class HrService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "stop_action" -> {
                stopForeground(true)
                stopSelf()
                stopHTTP()
            }
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, HrService::class.java).apply {
            action = "stop_action"
        }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_MUTABLE)

        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val notification: Notification = NotificationCompat.Builder(this, "ForegroundServiceChannel")
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop service", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if(isRunning()) {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val channelId = "ForegroundServiceChannel"
        private const val FOREGROUND_NOTIFICATION_ID = 1

        fun isForegroundServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)

            for (serviceInfo in runningServices) {
                if (serviceClass.name == serviceInfo.service.className) {
                    return true
                }
            }
            return false
        }
    }
}
