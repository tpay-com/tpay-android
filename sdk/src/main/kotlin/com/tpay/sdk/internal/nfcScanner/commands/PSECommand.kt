package com.tpay.sdk.internal.nfcScanner.commands

internal class PSECommand(
    private val cardType: CardType
) : APDUCommand(clazz = 0x00, instruction = 0xA4.toByte(), p1 = 0x04, p2 = 0x00) {
    override fun getBytes(): ByteArray = super.getBytes() + cardType.bytes.size.toByte() + cardType.bytes + 0x00
}

internal enum class CardType(val bytes: ByteArray){
    CONTACT("1PAY.SYS.DDF01".toByteArray()),
    CONTACTLESS("2PAY.SYS.DDF01".toByteArray())
}
