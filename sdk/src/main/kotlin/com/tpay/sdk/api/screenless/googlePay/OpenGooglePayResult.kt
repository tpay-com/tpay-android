package com.tpay.sdk.api.screenless.googlePay

/**
 * Class indicating result of Google Pay module
 */
sealed class OpenGooglePayResult {
    /**
     * Indicates that payer has chosen credit card and
     * data was received successfully
     *
     * @param [token] information about credit card from Google Pay response
     * @param [description] credit card description, for example "Visa •••• 1111"
     * @param [cardNetwork] credit card network
     * @param [cardTail] last digits of credit card number
     */
    data class Success(
        val token: String,
        val description: String,
        val cardNetwork: String,
        val cardTail: String
    ) : OpenGooglePayResult()

    /**
     * Indicates that payer closed the Google Pay module
     * without selecting the credit card
     */
    object Cancelled : OpenGooglePayResult()

    /**
     * Indicates that there was a error while parsing data or
     * receiving activity result code
     */
    object UnknownError : OpenGooglePayResult()
}