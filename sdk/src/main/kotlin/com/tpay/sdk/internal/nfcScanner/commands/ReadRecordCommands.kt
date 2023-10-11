package com.tpay.sdk.internal.nfcScanner.commands

import com.tpay.sdk.internal.nfcScanner.models.AFLRecord

internal class ReadRecordCommands(private val aflRecord: AFLRecord){
    private val commandStart = byteArrayOf(0x00, 0xB2.toByte())
    fun getCommands(): List<ByteArray> {
        val commands = mutableListOf<ByteArray>()
        for (recordIndex in (aflRecord.startIndex..aflRecord.endIndex)) {
            commands.add(commandStart + byteArrayOf(recordIndex.toByte(), aflRecord.sfi, 0x00))
        }
        return commands
    }
}