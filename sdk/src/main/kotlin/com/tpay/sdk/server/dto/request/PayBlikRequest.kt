package com.tpay.sdk.server.dto.request

import com.tpay.sdk.server.dto.parts.BLIKPaymentDTO

internal class PayBlikRequest(
    paymentData: BLIKPaymentDTO
) : PayTransactionRequestDTO() {
    init {
        groupId = CreateTransactionRequestDTO.BLIK_GROUP_ID
        blikPaymentData = paymentData
    }
}