package com.bpmlinks.vbank.twilio

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vbank.vidyovideoview.R
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.fullscreenintent.turnScreenOnAndKeyguardOff
import com.vbank.vidyovideoview.fullscreenintent.workmanager.DeclineCallWorker
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.connecting_layout.img_connecting_circle


class CallActivity : AppCompatActivity() {

    private var rotate: Animation? = null
    private val handler: Handler? = Handler()

    private var meetingParams : MeetingParams? = null

    companion object {
         const val CANCEL_NOTIFICATION: Long = 60000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        turnScreenOnAndKeyguardOff()
        meetingParams = if (intent.hasExtra(BundleKeys.MeetingParams)) {
            intent.getParcelableExtra(BundleKeys.MeetingParams) as MeetingParams
        }else{
            MeetingParams()
        }

        txt_banker_name.text = meetingParams?.bankerName
        txt_title.text = meetingParams?.title

        startTimerToFinish()
        startConnectingAnimation()
        ivAccept.setOnClickListener {
            removeNotification()
            val videoIntent = Intent(this@CallActivity, VideoActivity::class.java)
            if (intent.hasExtra(BundleKeys.MeetingParams)) {
                videoIntent.putExtra(BundleKeys.MeetingParams, meetingParams)
            }
            startActivity(videoIntent)
            finish()
        }
        ivReject.setOnClickListener {
            declineCall()
            removeNotification()
            finish()
        }
        setActivityBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun setActivityBackgroundColor(color: Int) {
        val view: View = this.window.decorView
        view.setBackgroundColor(color)
    }

    private fun startConnectingAnimation() {
        rotate = AnimationUtils.loadAnimation(this@CallActivity, R.anim.rotate)
        img_connecting_circle.startAnimation(rotate)
    }

    private fun stopConnectingAnimation() {
        rotate?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeNotification()
        stopConnectingAnimation()
        stopTimerToFinish()
        //turnScreenOffAndKeyguardOn()
    }

    private fun removeNotification() {
        if (intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }

    private fun declineCall() {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<DeclineCallWorker>()
        val data = Data.Builder()
        data.putInt(BundleKeys.CallKeyNb, meetingParams?.callKeyNb ?: 0)
        data.putInt(BundleKeys.CustomerKeyNb,meetingParams?.customerKeyNb ?: 0)
        data.putString(BundleKeys.CallEndReason, BundleKeys.callDecline)
        uploadWorkRequest.setInputData(data.build())
        WorkManager
            .getInstance(this@CallActivity)
            .enqueue(uploadWorkRequest.build())
    }

    private fun cancelNotification(id: Int) {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)
        finish()
    }


    private fun startTimerToFinish() {
        handler?.postDelayed(runnable, CANCEL_NOTIFICATION)
    }

    private fun stopTimerToFinish() {
        handler?.removeCallbacks(runnable)
    }

    private val runnable = Runnable {
        cancelNotification(1)
    }
}
