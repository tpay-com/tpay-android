package com.tpay.sdk.extensions

import java.text.SimpleDateFormat
import java.util.*

internal fun Date.toString(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return sdf.format(this)
}