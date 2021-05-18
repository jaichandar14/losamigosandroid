package com.bpmlinks.vbank.extension

import android.content.SharedPreferences

/**
 * Inline function to handle preference editor
 */
inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.func()
    editor.apply()
}