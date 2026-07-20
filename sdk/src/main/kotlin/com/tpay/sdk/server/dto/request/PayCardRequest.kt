package com.tpay.sdk.server.dto.request

import com.tpay.sdk.server.dto.parts.CardPaymentDTO

internal class PayCardRequest(
    paymentData: CardPaymentDTO,
    channelId: Int
) : PayTransactionRequestDTO() {
    init {
        this.channelId = channelId
        cardPaymentData = paymentData
    }
}