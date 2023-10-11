package com.tpay.sdk.internal.nfcScanner.commands

internal class SelectApplicationCommand(
    private val applicationId: ByteArray
) : APDUCommand(clazz = 0x00, instruction = 0xA4.toByte(), p1 = 0x04, p2 = 0x00) {
    override fun getBytes(): ByteArray = super.getBytes() + applicationId.size.toByte() + applicationId + 0x00
}