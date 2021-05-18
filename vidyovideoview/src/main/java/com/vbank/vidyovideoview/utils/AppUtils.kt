package com.vbank.vidyovideoview.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.vbank.vidyovideoview.BuildConfig
import com.vbank.vidyovideoview.connector.ConnectParams
import java.io.File

class AppUtils {

    companion object{
        private const val LOGS_FOLDER = "vidyovideoviewLogs"
        private const val LOG_FILE = "vidyovideoviewLog.log"

        fun formatToken(): String? {
            val token: String = ConnectParams.TOKEN
            if (TextUtils.isEmpty(token)) return ""
            return token.substring(0, 8) + "..."
        }

        /**
         * Log file is create individually for every session
         *
         * @param context [Context]
         * @return log file path
         */
        fun configLogFile(context: Context): String? {
            val cacheDir = context.cacheDir
            val logDir = File(cacheDir, LOGS_FOLDER)
            AppUtils.deleteRecursive(logDir)
            val logFile = File(logDir, LOG_FILE)
            logFile.mkdirs()
            val logFiles = logDir.list()
            if (logFiles != null) for (file in logFiles) Logger.i(AppUtils::class.java, "Cached log file: $file")
            return logFile.absolutePath
        }

        /**
         * Expose log file URI for sharing.
         *
         * @param context [Context]
         * @return log file uri.
         */
        private fun logFileUri(context: Context): Uri? {
            val cacheDir = context.cacheDir
            val logDir = File(cacheDir, LOGS_FOLDER)
            val logFile = File(logDir, LOG_FILE)
            return if (!logFile.exists()) null else FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID.toString() + ".file.provider", logFile)
        }

        private fun deleteRecursive(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
            fileOrDirectory.delete()
        }

        /**
         * Send email with log file
         */
        fun sendLogs(context: Context) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Vidyo Connector Sample Logs")
            intent.putExtra(Intent.EXTRA_TEXT, "Logs attached..." + AppUtils.additionalInfo())
            intent.putExtra(Intent.EXTRA_STREAM, logFileUri(context))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(Intent.createChooser(intent, "Choose sender..."))
            } catch (sendReportEx: Exception) {
                sendReportEx.printStackTrace()
            }
        }

        private fun additionalInfo(): String? {
            return "\n\nModel: " + Build.MODEL +
                    "\n" + "Manufactured: " + Build.MANUFACTURER +
                    "\n" + "Brand: " + Build.BRAND +
                    "\n" + "Android OS version: " + Build.VERSION.RELEASE +
                    "\n" + "Hardware : " + Build.HARDWARE +
                    "\n" + "SDK Version : " + Build.VERSION.SDK_INT
        }

        fun isLandscape(resources: Resources): Boolean {
            return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        fun <T> dump(list: List<T>?) {
            list?.let {
                for (t in list) Logger.i("Item: %s", t.toString())
            }
        }
    }
}