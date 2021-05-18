package com.bpmlinks.vbank.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * Used to start the activity through Activity
 */
inline fun <reified T : Any> Activity.launchActivityForResult(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {

    val intent = newIntent<T>(this)
    intent.init()
    options?.let {
        intent.putExtras(options)
    }
    startActivityForResult(intent, requestCode, options)
}

/**
 * Used to start the activity through Context
 */
inline fun <reified T : Any> Activity.launchActivity(
        options: Bundle? = null,
        canFinish: Boolean = false,
        noinline init: Intent.() -> Unit = {}) {

    val intent = newIntent<T>(this)
    intent.init()
    options?.let {
        intent.putExtras(options)
    }
    startActivity(intent)
    if (canFinish) this.finish()
}

/**
 * helper method
 */
inline fun <reified T : Any> newIntent(context: Context): Intent =
        Intent(context, T::class.java)

@SuppressLint("MissingPermission")
        /**
         * Extenstion function to call
         */
fun Activity.call(mobileNo: String?) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("""tel:$mobileNo"""))
    startActivity(intent)
}