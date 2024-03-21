package com.tpay.sdk.api.screenless.channelMethods

sealed class GetPaymentChannelsResult {
    /**
     * Indicates that request was successful
     * @param [language] language code
     * @param [currency] currency code
     * @param [channels] received channels
     */
    data class Success(
        val language: String,
        val currency: String,
        val channels: List<PaymentChannel>
    ) : GetPaymentChannelsResult()

    /**
     * Indicates that request failed
     *
     * @param [devErrorMessage] optional error message
     */
    data class Error(val devErrorMessage: String?) : GetPaymentChannelsResult()
}
