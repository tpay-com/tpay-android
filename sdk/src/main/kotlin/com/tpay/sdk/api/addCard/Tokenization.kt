package com.tpay.sdk.api.addCard

import com.tpay.sdk.api.models.payer.Payer

/**
 * Class responsible for storing data about tokenization
 * @param [payer] data about payer
 * @param [notificationUrl] url that tokenization notifications will be sent to
 */
data class Tokenization(
    val payer: Payer,
    val notificationUrl: String
)
