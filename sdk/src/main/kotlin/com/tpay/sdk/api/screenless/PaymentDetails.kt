package com.tpay.sdk.api.screenless

import com.tpay.sdk.api.models.Language

/**
 * Class responsible for storing basic information about payment.
 *
 * @param [amount] payment amount/price
 * @param [description] description visible for payer
 * @param [hiddenDescription] hidden description visible for merchant
 * @param [language] language used in payment
 */
data class PaymentDetails(
    val amount: Double,
    val description: String,
    val hiddenDescription: String? = null,
    val language: Language? = null
)
