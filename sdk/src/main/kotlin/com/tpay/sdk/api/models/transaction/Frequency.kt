package com.tpay.sdk.api.models.transaction

/**
 * Enum defining frequency of recurring payments
 */
enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY;

    companion object {
        val Frequency.code: Int
            get() = ordinal + 1
    }
}
