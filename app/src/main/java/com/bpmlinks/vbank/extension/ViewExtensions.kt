package com.bpmlinks.vbank.extension

import android.content.Context
import android.view.View
import android.widget.TextView

/**
 * Used to get context of the view
 */
val View.ctx: Context
    get() = context

/**
 * Set TextView color
 */
var TextView.textColor: Int
    get() = currentTextColor
    set(v) = setTextColor(v)