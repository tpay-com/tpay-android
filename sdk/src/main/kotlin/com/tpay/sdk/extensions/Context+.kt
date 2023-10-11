package com.tpay.sdk.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.nfc.NfcAdapter
import androidx.core.content.ContextCompat

// Resources
internal fun Context.color(color: Int): Int {
    return ContextCompat.getColor(this, color)
}

internal fun Context.drawable(drawable: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawable)
}

// NFC
internal fun Context.isNFCAvailable() = NfcAdapter.getDefaultAdapter(this) != null