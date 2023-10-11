package com.tpay.sdk.internal.nfcScanner.extensions

internal fun Byte.getBit(position: Int): Int {
    return (this.toInt() shr position) and 1
}