package com.tpay.sdk.api.models

import com.tpay.sdk.api.models.payer.Payer

/**
 * Class responsible for storing information about payer and his
 * automatic payment methods
 */
data class PayerContext(
    val payer: Payer,
    val automaticPaymentMethods: AutomaticPaymentMethods? = null
)
