package com.tpay.sdk.internal.nfcScanner.models

internal data class AFLRecord(
    val sfi: Byte,
    val startIndex: Byte,
    val endIndex: Byte
)
