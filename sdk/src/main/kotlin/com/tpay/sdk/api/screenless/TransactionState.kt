package com.tpay.sdk.api.screenless

/**
 * Enum defining state of transaction
 */
enum class TransactionState(val actual: String) {
    PAID("paid"),
    CORRECT("correct"),
    PENDING("pending"),
    ERROR("error"),
    REFUND("refund"),
    DECLINED("declined"),
    UNKNOWN("unknown");

    companion object {
        val SUCCESS_STATES = listOf(PAID.actual, CORRECT.actual)
    }
}
