package com.tpay.sdk.internal.nfcScanner.commands

import com.tpay.sdk.internal.nfcScanner.models.EMVTag
import java.util.*
import kotlin.random.Random

internal class GetProcessingOptionsWithPDOLCommand(
    private val pdolTags: List<EMVTag>
) : APDUCommand(clazz = 0x80.toByte(), instruction = 0xA8.toByte(), p1 = 0x00, p2 = 0x00) {
    override fun getBytes(): ByteArray {
        var result = byteArrayOf()
        pdolTags.forEach { pdolTag ->
            result += when(pdolTag){
                EMVTag.TERMINAL_TRANSACTION_QUALIFIERS -> byteArrayOf(0xB6.toByte(), 0x20, 0xC0.toByte(), 0x00)
                EMVTag.AMOUNT_AUTHORISED -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x10)
                EMVTag.AMOUNT_OTHER -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x10)
                EMVTag.TERMINAL_COUNTRY_CODE -> byteArrayOf(0x06, 0x16)
                EMVTag.TRANSACTION_CURRENCY_CODE -> byteArrayOf(0x09, 0x85.toByte())
                EMVTag.TRANSACTION_VERIFICATION_RESULTS -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
                EMVTag.TRANSACTION_DATE -> {
                    val calendar = GregorianCalendar.getInstance(Locale.getDefault())
                    val year = calendar.get(GregorianCalendar.YEAR)
                    val month = calendar.get(GregorianCalendar.MONTH) + 1
                    val day = calendar.get(GregorianCalendar.DAY_OF_MONTH)
                    byteArrayOf(year.toString().substring(2).toByte(), month.toByte(), day.toByte())
                }
                EMVTag.TRANSACTION_TYPE -> byteArrayOf(0x00)
                EMVTag.TRANSACTION_TIME -> {
                    val calendar = GregorianCalendar.getInstance(Locale.getDefault())
                    val hour = calendar.get(GregorianCalendar.HOUR_OF_DAY)
                    val minute = calendar.get(GregorianCalendar.MINUTE)
                    val second = calendar.get(GregorianCalendar.SECOND)
                    byteArrayOf(hour.toByte(), minute.toByte(), second.toByte())
                }
                EMVTag.UNPREDICTABLE_NUMBER -> Random.nextBytes(4)
                EMVTag.TERMINAL_CAPABILITIES -> byteArrayOf(0xE0.toByte(), 0xF8.toByte(), 0xE8.toByte())
                EMVTag.ADDITIONAL_TERMINAL_CAPABILITIES -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
                EMVTag.TERMINAL_TYPE -> byteArrayOf(22.toByte())
                else -> { byteArrayOf() }
            }
        }

        return super.getBytes() + byteArrayOf((result.size + 2).toByte()) + byteArrayOf(0x83.toByte(), result.size.toByte()) + result + byteArrayOf(0x00)
    }
}