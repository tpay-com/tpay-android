package com.tpay.sdk.internal.model

import com.tpay.sdk.server.dto.parts.TransactionMethodDTO

data class TransactionMethod(
    val id: String,
    val name: String,
    val imageUrl: String,
    val paymentChannels: List<String>,
    val mainPaymentChannel: String
) {
    companion object {
        internal fun fromDTO(transactionMethodDTO: TransactionMethodDTO): TransactionMethod {
            return TransactionMethod(
                id = transactionMethodDTO.id,
                name = transactionMethodDTO.name,
                imageUrl = transactionMethodDTO.img,
                paymentChannels = transactionMethodDTO.availablePaymentChannels,
                mainPaymentChannel = transactionMethodDTO.mainChannel
            )
        }
    }
}
