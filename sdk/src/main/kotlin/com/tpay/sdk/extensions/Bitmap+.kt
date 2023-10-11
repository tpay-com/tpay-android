package com.tpay.sdk.extensions

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

internal fun ByteArray.toBitmapDrawable(): BitmapDrawable {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, size)
    return BitmapDrawable(Resources.getSystem(), bitmap)
}