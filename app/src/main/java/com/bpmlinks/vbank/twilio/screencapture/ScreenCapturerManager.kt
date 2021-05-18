package com.bpmlinks.vbank.twilio.screencapture

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

@TargetApi(29)
class ScreenCapturerManager internal constructor(private val mContext: Context) {
    private var mService: ScreenCapturerService? = null
    private var currentState =
        State.UNBIND_SERVICE

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            // We've bound to ScreenCapturerService, cast the IBinder and get ScreenCapturerService instance
            val binder: ScreenCapturerService.LocalBinder =
                service as ScreenCapturerService.LocalBinder
            mService = binder.service
            currentState = State.BIND_SERVICE
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    /**
     * An enum describing the possible states of a ScreenCapturerManager.
     */
    enum class State {
        BIND_SERVICE, START_FOREGROUND, END_FOREGROUND, UNBIND_SERVICE
    }

    private fun bindService() {
        val intent = Intent(mContext, ScreenCapturerService::class.java)
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun startForeground() {
        mService?.startForeground()
        currentState = State.START_FOREGROUND

    }

    fun endForeground() {
        mService?.endForeground()
        currentState = State.END_FOREGROUND
    }

    fun unbindService() {
        mContext.unbindService(connection)
        currentState = State.UNBIND_SERVICE
    }

    init {
        bindService()
    }
}