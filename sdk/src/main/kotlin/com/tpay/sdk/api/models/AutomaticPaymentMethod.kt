package com.tpay.sdk.api.models

import com.tpay.sdk.api.paycard.CreditCardBrand

/**
 * Class responsible for storing automatic payment methods.
 * @param [blikAlias] BLIK alias that will be used to register/pay (if registered)
 * @param [tokenizedCards] tokenized cards created after successful credit card payment
 */
class AutomaticPaymentMethods(
    val blikAlias: BlikAlias? = null,
    val tokenizedCards: List<TokenizedCard> = emptyList()
)

/**
 * Class responsible for storing tokenized card data
 * @param [token] credit card token
 * @param [cardTail] 4 last numbers of credit card number
 * @param [brand] credit card brand
 */
data class TokenizedCard(
    val token: String,
    val cardTail: String,
    val brand: CreditCardBrand
)

/**
 * Class responsible for storing BLIK alias data
 */
sealed class BlikAlias(val value: String, val label: String?) {
    /**
     * Indicates that BLIK alias is registered and ready to be used as a payment method.
     */
    class Registered(value: String, label: String? = null) : BlikAlias(value, label)

    /**
     * Indicates that BLIK alias needs to be registered with standard 6-digit BLIK payment
     */
    class NotRegistered(value: String, label: String? = null) : BlikAlias(value, label)
}