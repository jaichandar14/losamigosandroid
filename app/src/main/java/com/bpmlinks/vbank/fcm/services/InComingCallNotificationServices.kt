package com.bpmlinks.vbank.fcm.services

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Ringtone
import android.media.audiofx.Virtualizer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.twilio.VideoActivity
import com.vbank.vidyovideoview.fullscreenintent.CallActivity

class InComingCallNotificationServices : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null && intent.extras != null && intent.hasExtra(AppConstants.ACTION_SHOW_CALL)) {
            showActivity()
        }
        return START_NOT_STICKY
    }

    private fun showActivity() {
        //stopForeground(true)
        val intent = Intent(this, CallActivity::class.java)
        startActivity(intent)
    }

    private fun createNotification(
        notificationId: Int,
        channelImportance: Int
    ): Notification? {
        val intent = Intent(this, VideoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        val extras = Bundle()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotification(
                "",
                pendingIntent,
                extras,
                notificationId,
                createChannel(channelImportance)
            )
        } else {
            NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_call_end_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(" is calling.")
                .setAutoCancel(true)
                .setExtras(extras)
                .setContentIntent(pendingIntent)
                .setGroup("test_app_notification")
                .setColor(Color.rgb(214, 10, 37)).build()
        }
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun buildNotification(
        text: String, pendingIntent: PendingIntent, extras: Bundle,
        notificationId: Int,
        channelId: String
    ): Notification? {
        val rejectIntent =
            Intent(applicationContext, VideoActivity::class.java)
        val piRejectIntent = PendingIntent.getService(
            applicationContext,
            0,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val acceptIntent =
            Intent(applicationContext, VideoActivity::class.java)
        val piAcceptIntent = PendingIntent.getService(
            applicationContext,
            0,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder: Notification.Builder = Notification.Builder(applicationContext, channelId)
                .setSound(Settings.System.DEFAULT_RINGTONE_URI)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setCategory(Notification.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setExtras(extras)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_call_end_24dp,
                "Decline",
                piRejectIntent
            )
            .addAction(R.drawable.ic_call_24dp, "Answer", piAcceptIntent)
            .setFullScreenIntent(pendingIntent, true)
                        return builder.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(channelImportance: Int): String {
        var callInviteChannel = NotificationChannel(
            "AppConstants.VOICE_CHANNEL_HIGH_IMPORTANCE",
            "Primary Voice Channel", NotificationManager.IMPORTANCE_HIGH
        )
        var channelId: String = "AppConstants.VOICE_CHANNEL_HIGH_IMPORTANCE"
        if (channelImportance == NotificationManager.IMPORTANCE_LOW) {
            callInviteChannel = NotificationChannel(
                "AppConstants.VOICE_CHANNEL_LOW_IMPORTANCE",
                "Primary Voice Channel", NotificationManager.IMPORTANCE_LOW
            )
            channelId = "AppConstants.VOICE_CHANNEL_LOW_IMPORTANCE"
        }
        callInviteChannel.lightColor = Color.GREEN
        callInviteChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(callInviteChannel)
        return channelId
    }
}