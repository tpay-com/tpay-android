package com.tpay.sdk.extensions

import android.os.Handler
import android.os.Looper

fun runDelayedOnMainThread(action: () -> Unit, duration: Long) {
    Handler(Looper.getMainLooper()).postDelayed(action, duration)
}

fun runOnMainThread(action: () -> Unit) {
    Handler(Looper.getMainLooper()).post(action)
}
