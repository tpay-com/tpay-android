package com.tpay.sdk.internal


internal object PaymentStatus {
    const val ERROR_STATUS = "error"
    private const val PAID_STATUS = "paid"
    private const val CORRECT_STATUS = "correct"
    val SUCCESS_STATUSES = listOf(PAID_STATUS, CORRECT_STATUS)
}