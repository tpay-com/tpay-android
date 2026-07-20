package com.tpay.sdk.server.dto.request

internal class PayGooglePayRequest(
    token: String,
    channelId: Int
) : PayTransactionRequestDTO() {
    init {
        this.channelId = channelId
        googlePayPaymentData = token
    }
}