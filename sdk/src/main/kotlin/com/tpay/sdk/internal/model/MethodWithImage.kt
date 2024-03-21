package com.tpay.sdk.internal.model

import android.graphics.drawable.Drawable

internal data class MethodWithImage(
    val channelId: Int,
    val name: String,
    val image: Drawable? = null
)
