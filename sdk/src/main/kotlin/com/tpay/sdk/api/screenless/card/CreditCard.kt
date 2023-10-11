package com.tpay.sdk.api.screenless.card

/**
 * Class responsible for storing information about credit card
 */
data class CreditCard(
    val cardNumber: String,
    val expirationDate: String,
    val cvv: String
)
