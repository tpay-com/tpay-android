package com.tpay.sdk.extensions

import android.content.res.Resources

internal val Float.px: Float
    get() = this * Resources.getSystem().displayMetrics.density