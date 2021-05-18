package com.bpmlinks.vbank.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.fcm.receiver.NotificationReceiver
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.helper.AppPreferences
import com.bpmlinks.vbank.ui.videorecordpermission.VdRecordPermissionActivity
import com.bpmlinks.vbank.ui.weview.WebViewActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.fullscreenintent.CallActivity
import com.vbank.vidyovideoview.helper.BundleKeys

class FirebaseNotification : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppPreferences.getInstance()
            .setStringValue(applicationContext, AppPreferences.FCM_TOKEN, token)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("call receive"," call receive first")
        if (remoteMessage.data.isNotEmpty()) {
            notifications(remoteMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun notifications(remoteMessage: RemoteMessage) {
        //val notificationId = AtomicInteger(0).incrementAndGet()
        val notificationId = 1

        val intent=   if (remoteMessage.data["docuSignUrl"].isNullOrEmpty())
        {
            Intent(this, VdRecordPermissionActivity::class.java)
        }
        else
        {
            Intent(this, WebViewActivity::class.java)
        }



        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        val meetingParams = MeetingParams()



        if (!remoteMessage.data["docuSignUrl"].isNullOrEmpty()){
            try {
                meetingParams.docusignurl = remoteMessage.data["docuSignUrl"]?.toString()
            }catch (e : NumberFormatException){
                e.printStackTrace()
            }
        }

        if (!remoteMessage.data["callKeyNb"].isNullOrEmpty()){
            try {
                meetingParams.callKeyNb = remoteMessage.data["callKeyNb"]?.toInt()
            }catch (e : NumberFormatException){
                e.printStackTrace()
            }
        }
//        if (!remoteMessage.data["envolpeId"].isNullOrEmpty()){
//            try {
//                meetingParams.envolpeId = remoteMessage.data["envolpeId"]?.toInt()
//            }catch (e : NumberFormatException){
//                e.printStackTrace()
//            }
//        }
        if (!remoteMessage.data["vidyoToken"].isNullOrEmpty()){
            meetingParams.token = remoteMessage.data["vidyoToken"].toString()
        }

        if (!remoteMessage.data["customerKeyNb"].isNullOrEmpty()){
            try {
                meetingParams.customerKeyNb = remoteMessage.data["customerKeyNb"]?.toInt()
            }catch (e : NumberFormatException){
                e.printStackTrace()
            }
        }
        var bankerName : String? = null

        if (!remoteMessage.data["bankerName"].isNullOrEmpty()){
            meetingParams.bankerName = remoteMessage.data["bankerName"].toString()
            bankerName = remoteMessage.data["bankerName"].toString()
        }
        if (!remoteMessage.data["roomId"].isNullOrEmpty()){
            meetingParams.resource = remoteMessage.data["roomId"].toString()
        }
        if (!remoteMessage.data["customerName"].isNullOrEmpty()){
            meetingParams.displayName = remoteMessage.data["customerName"].toString()
        }
        var title : String? = getString(R.string.app_name)
        if (!remoteMessage.data["title"].isNullOrEmpty()){
            title = remoteMessage.data["title"].toString()
            meetingParams.title = remoteMessage.data["title"].toString()
        }
        if (!remoteMessage.data["roomName"].isNullOrEmpty()){
            meetingParams.roomName = remoteMessage.data["roomName"].toString()
        }

        meetingParams.isFromNotification = true

        Log.d("NOTIFICATION_OBJECT",meetingParams.toString())
        Log.d("NOTIFICATIO_TOKEN",meetingParams.token)
        Log.d("NOTIFICATION_TITLE",meetingParams.title)

        intent.putExtra(BundleKeys.MeetingParams, meetingParams)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val fullScreenIntent = Intent(this, com.bpmlinks.vbank.twilio.CallActivity::class.java)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        fullScreenIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        fullScreenIntent.putExtra(BundleKeys.MeetingParams, meetingParams)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val buttonIntent = Intent(baseContext, NotificationReceiver::class.java)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_CUSTOMER_KEY, meetingParams.customerKeyNb)
        buttonIntent.putExtra(AppConstants.NOTIFICATION_CALL_KEY, meetingParams.callKeyNb)

        val dismissIntent =
            PendingIntent.getBroadcast(baseContext, 0, buttonIntent, 0)

        val channelId = getString(R.string.app_name)
        val ringTonePath: Uri = Uri.parse("android.resource://$packageName/raw/grabcall_dialling")

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
        if(!remoteMessage.data["docuSignUrl"].isNullOrEmpty()) {

         /*   val notificationLayout = RemoteViews(this?.packageName, R.layout.custome_notification)
            notificationLayout.setOnClickPendingIntent(R.id.action_btn,pendingIntent)
            notificationLayout.setTextViewText(R.id.custome_notification_title,title)*/


     /*      builder ?.setSmallIcon(R.mipmap.app_icon)
                ?.setContentTitle(title)
                ?.setContentText(bankerName)
                ?.setAutoCancel(false)
                ?.setOngoing(true)
                ?.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                ?.setCustomContentView(notificationLayout)
                ?.setCustomBigContentView(notificationLayout)*/

            builder ?.setSmallIcon(R.mipmap.ic_launcher)
                ?.setContentTitle(title)
                ?.setContentText(bankerName)
                ?.setAutoCancel(false)
                ?.setOngoing(true)
//                ?.addAction(
//                    0,
//                    getString(R.string.btn_accept_action),
//                    pendingIntent
//                )


        }
        else {
            builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(bankerName)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setCategory(Notification.CATEGORY_CALL)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSound(ringTonePath)
                .addAction(
                    R.drawable.ic_call_end_24dp,
                    getString(R.string.btn_reject_incoming_call),
                    dismissIntent
                )
                .addAction(
                    R.drawable.ic_call_24dp,
                    getString(R.string.btn_accept_incoming_call),
                    pendingIntent
                )
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.lightColor = Color.GREEN
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            if(!remoteMessage.data["docuSignUrl"].isNullOrEmpty())
            {
                channel.setSound(ringTonePath,attributes)
            }

            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notification.flags = Notification.FLAG_INSISTENT

        notificationManager.notify(notificationId, notification)
        removeNotification(notificationId)

        if (remoteMessage.data["docuSignUrl"].isNullOrEmpty())
        {
            var brodcostintent=Intent(getString(R.string.brodcost_recever))
            brodcostintent.putExtra(BundleKeys.MeetingParams,meetingParams)
            brodcostintent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
            LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)
        }
        else
        {
            var brodcostintent=Intent(getString(R.string.docusign_brodcost_recever))
            brodcostintent.putExtra(BundleKeys.docusignurl,remoteMessage.data["docuSignUrl"])
            brodcostintent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)
            LocalBroadcastManager.getInstance(this).sendBroadcast(brodcostintent)
        }
    }

    private fun removeNotification(id : Int){
        Handler(Looper.getMainLooper()).postDelayed({
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(id)
        },CallActivity.CANCEL_NOTIFICATION)
    }

    private fun isAppVisible(): Boolean {
        return ProcessLifecycleOwner
            .get()
            .lifecycle
            .currentState
            .isAtLeast(Lifecycle.State.STARTED)
    }
}