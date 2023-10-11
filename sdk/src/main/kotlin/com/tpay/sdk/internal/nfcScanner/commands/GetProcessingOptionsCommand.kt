package com.tpay.sdk.internal.nfcScanner.commands

internal class GetProcessingOptionsCommand : APDUCommand(clazz = 0x80.toByte(), instruction = 0xA8.toByte(), p1 = 0x00, p2 = 0x00) {
    override fun getBytes(): ByteArray = super.getBytes() + byteArrayOf(0x02, 0x83.toByte(), 0x00, 0x00)
}