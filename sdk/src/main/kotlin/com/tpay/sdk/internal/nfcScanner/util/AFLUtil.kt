package com.tpay.sdk.internal.nfcScanner.util

import com.tpay.sdk.internal.nfcScanner.models.AFLRecord
import com.tpay.sdk.internal.nfcScanner.commands.ReadRecordCommands

internal class AFLUtil {
    companion object {
        internal fun getRecordCommands(aflRecords: List<AFLRecord>): List<ByteArray> {
            val commands = mutableListOf<ByteArray>()
            aflRecords.forEach { aflRecord ->
                commands.addAll(ReadRecordCommands(aflRecord).getCommands())
            }
            return commands
        }
    }
}