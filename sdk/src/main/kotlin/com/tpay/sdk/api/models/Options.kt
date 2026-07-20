package com.tpay.sdk.api.models

sealed class Option {
    data class SingleTransactionOnly(val value: Boolean) : Option()
}