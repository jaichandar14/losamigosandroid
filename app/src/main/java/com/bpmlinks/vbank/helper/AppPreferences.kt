package com.bpmlinks.vbank.helper

import android.content.Context
import androidx.preference.PreferenceManager
import com.bpmlinks.vbank.BuildConfig

class AppPreferences {
    companion object {
        private var appPreferences: AppPreferences? = null
        fun getInstance(): AppPreferences {
            if (appPreferences == null) {
                appPreferences = AppPreferences()
            }
            return appPreferences as AppPreferences
        }

        const val FCM_TOKEN = "FCM_TOKEN"

    }


    fun setStringValue(context: Context, key: String?, value: String?) {
        val sharedPreferences = context.getSharedPreferences(
            BuildConfig.preference_name, Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringValue(ctx: Context?, key: String?): String? {
        return if (ctx != null) {
            val sharedPreferences = ctx.getSharedPreferences(
                BuildConfig.preference_name, Context.MODE_PRIVATE
            )
            sharedPreferences.getString(key, "")
        } else {
            ""
        }
    }

    fun clearPreferences(ctx: Context?) {
        val sharedPreferences = ctx?.getSharedPreferences(
            BuildConfig.preference_name, Context.MODE_PRIVATE
        )
        sharedPreferences?.edit()?.clear()?.apply()
    }
}