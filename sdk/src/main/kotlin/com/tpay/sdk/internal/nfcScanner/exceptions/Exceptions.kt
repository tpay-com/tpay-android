package com.tpay.sdk.internal.nfcScanner.exceptions

internal class InvalidCreditCardResponseException(override val message: String?) : Exception()
internal class InvalidEMVDataFormatException(override val message: String?) : Exception()