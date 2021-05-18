package com.vbank.vidyovideoview.utils

import android.util.Log

class Logger {
    enum class LogType {
        ERROR, INFO, WARNING
    }

    companion object {
        private val ENABLED: Boolean = com.vbank.vidyovideoview.BuildConfig.DEBUG

        private const val TAG = "vidyovideoview"

        fun e(error: String?) {
            log(null, error, LogType.ERROR)
        }

        fun e(cls: Class<*>?, error: String?) {
            log(cls, error, LogType.ERROR)
        }

        fun i(info: String?) {
            log(null, info, LogType.INFO)
        }

        fun i(cls: Class<*>?, info: String?) {
            log(cls, info, LogType.INFO)
        }

        fun i(cls: Class<*>?, info: String?, vararg params: Any?) {
            log(cls, String.format(info!!, *params), LogType.INFO)
        }

        fun i(info: String?, vararg params: Any?) {
            log(null, String.format(info!!, *params), LogType.INFO)
        }

        fun w(warning: String?) {
            log(null, warning, LogType.WARNING)
        }

        fun w(cls: Class<*>?, warning: String?) {
            log(cls, warning, LogType.WARNING)
        }

        private fun log(cls: Class<*>?, message: String?, logType: LogType) {
            val builder = StringBuilder()
            if (cls != null) {
                builder.append(cls.simpleName)
                builder.append(": ")
            }
            if (message != null) {
                builder.append(message)
            }
            val out = builder.toString()
            if (ENABLED) return
            when (logType) {
                LogType.ERROR -> Log.e(TAG, out)
                LogType.WARNING -> Log.w(TAG, out)
                LogType.INFO -> Log.i(TAG, out)
            }
        }
    }
}