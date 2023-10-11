package com.tpay.sdk.internal.nfcScanner

import com.tpay.sdk.internal.nfcScanner.models.CreditCardInformation


internal data class PayCardScanResult(private val records: List<ByteArray>) {
    fun getCreditCardInformation(): CreditCardInformation? = CreditCardInformation.from(records)
}