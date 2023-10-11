package com.tpay.sdk.extensions

import android.content.res.Resources

internal val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

internal fun Int.formatMonth(): String {
    return if(this < 10){
        "0$this"
    } else "$this"
}

internal fun Int.formatYearLast2Digits(): String = toString().substring(2)