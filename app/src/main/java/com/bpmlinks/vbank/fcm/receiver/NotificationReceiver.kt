package com.bpmlinks.vbank.fcm.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bpmlinks.vbank.fcm.FirebaseNotification
import com.bpmlinks.vbank.helper.AppConstants

import com.vbank.vidyovideoview.fullscreenintent.workmanager.DeclineCallWorker
import com.vbank.vidyovideoview.helper.BundleKeys
import kotlinx.android.synthetic.main.vehicle_inspection_fragment.*


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(AppConstants.NOTIFICATION_ID, 0)
        val customerKey = intent.getIntExtra(AppConstants.NOTIFICATION_CUSTOMER_KEY, 0)
//        var callKey = intent.getIntExtra(AppConstants.NOTIFICATION_CALL_KEY, 0)
        var callKey = FirebaseNotification.callKeyNbForDocOffline

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
        declineCall(context,customerKey,callKey)
    }

    private fun declineCall(
        context: Context,
        customerKey: Int,
        callKey: Int
    ) {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<DeclineCallWorker>()
        val data = Data.Builder()
        data.putInt(BundleKeys.CallKeyNb, callKey)
        data.putInt(BundleKeys.CustomerKeyNb,customerKey)
        data.putString(BundleKeys.CallEndReason, BundleKeys.callDecline)
        uploadWorkRequest.setInputData(data.build())
        WorkManager
            .getInstance(context)
            .enqueue(uploadWorkRequest.build())
    }
}