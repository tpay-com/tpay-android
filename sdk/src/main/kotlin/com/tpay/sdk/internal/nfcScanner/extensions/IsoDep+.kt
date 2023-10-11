package com.tpay.sdk.internal.nfcScanner.extensions

import android.nfc.tech.IsoDep

internal fun IsoDep.getRecordData(recordCommands: List<ByteArray>): List<ByteArray> {
    return recordCommands.map { transceive(it) }
}