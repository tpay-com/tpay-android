package com.tpay.sdk.internal.nfcScanner.commands


internal abstract class APDUCommand(
    val clazz: Byte,
    val instruction: Byte,
    val p1: Byte,
    val p2: Byte
) {
    open fun getBytes(): ByteArray = byteArrayOf(clazz, instruction, p1, p2)
}