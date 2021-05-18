package com.bpmlinks.vbank.twilio.screencapture

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bpmlinks.vbank.R

@TargetApi(29)
class ScreenCapturerService : Service() {
    // Binder given to clients
    private val binder: IBinder = LocalBinder()

    /**
     * Class used for the client Binder.  We know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of ScreenCapturerService so clients can call public methods
        val service: ScreenCapturerService
            get() =// Return this instance of ScreenCapturerService so clients can call public methods
                this@ScreenCapturerService
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun startForeground() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_NONE
        )
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationId = System.currentTimeMillis().toInt()
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_screen_share_white_24dp)
            .setContentTitle("ScreenCapturerService is running in the foreground")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(notificationId, notification)
    }

    fun endForeground() {
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    companion object {
        private const val CHANNEL_ID = "screen_capture"
        private const val CHANNEL_NAME = "Screen_Capture"
    }
}
