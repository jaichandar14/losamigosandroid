package com.bpmlinks.vbank.extension

import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern


fun String.isEmailValid(email:String): Boolean {
    val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(email)
    if (matcher.matches()) {
        return matcher.matches()
    } else {
        //txtFields.error = alert
       // requestFocus(txtFields, window)
        return matcher.matches()
    }
}

fun String.isValidMobileNumber(email:String): Boolean {
    return email.length==10
}

fun requestFocus(view: View, window: Window) {
    if (view.requestFocus()) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}

fun validateFieldsNotEmpty(
    txtFields: EditText, inputLayoutFields: TextInputLayout,
    alert: String, window: Window
): Boolean {
    if (txtFields.text.toString().trim { it <= ' ' }.isEmpty()) {
        inputLayoutFields.setError(alert)
        requestFocus(txtFields, window)
        return false
    } else {
        inputLayoutFields.setErrorEnabled(false)
    }
    return true
}

fun validatePassword(
    txtFields: EditText,
    alert: String, window: Window
): Boolean {
    val expression = "^(?=.*?\\d.*\\d)[a-zA-Z0-9]{8,}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(txtFields.text.toString())
    if (txtFields.text.toString().trim { it <= ' ' }.isEmpty()
        && txtFields.text.toString().trim().length < 8
        && !matcher.matches()) {
        txtFields.error = alert
        requestFocus(txtFields, window)
        return matcher.matches()
    } else {
       return true
    }
}

fun validateFieldsNotEmpty(
    txtFields: EditText,
    alert: String, window: Window
): Boolean {
    if (txtFields.text.toString().trim { it <= ' ' }.isEmpty()) {
        txtFields.error = alert
        requestFocus(txtFields, window)
        return false
    } else {
        txtFields.error = null
    }
    return true
}

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.isActiveNetworkMetered
}
