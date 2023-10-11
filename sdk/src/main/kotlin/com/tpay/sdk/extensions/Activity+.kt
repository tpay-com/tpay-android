package com.tpay.sdk.extensions

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

// Keyboard
internal fun Activity.hideKeyboard() {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.run {
        hideSoftInputFromWindow(window.decorView.applicationWindowToken, 0)
    }
}

// View binding
internal inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

@Suppress("DEPRECATION")
internal val Activity.screenHeight: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }.heightPixels
        }
    }

