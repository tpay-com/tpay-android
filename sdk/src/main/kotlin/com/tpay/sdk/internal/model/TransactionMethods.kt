package com.tpay.sdk.internal.model

import com.tpay.sdk.internal.paymentMethod.WalletMethod
import com.tpay.sdk.server.dto.response.GetTransactionMethodsResponseDTO


internal data class TransactionMethods(
    val cardPaymentAvailable: Boolean = false,
    val blikPaymentAvailable: Boolean = false,
    val transfers: List<TransactionMethod> = emptyList(),
    val wallets: List<WalletMethod> = emptyList()
) {
    companion object {
        fun fromDTO(getTransactionMethodsResponseDTO: GetTransactionMethodsResponseDTO): TransactionMethods {
            getTransactionMethodsResponseDTO.transactionMethodsDTO.transactionMethodDTOS
                .let { backendMethods ->
                    return TransactionMethods(
                        cardPaymentAvailable = backendMethods.firstOrNull { it.id == PaymentMethod.CREDIT_CARD.groupId } != null,
                        blikPaymentAvailable = backendMethods.firstOrNull { it.id == PaymentMethod.BLIK.groupId } != null,
                        wallets = backendMethods
                            .filter { listOf(PaymentMethod.GOOGLE_PAY.groupId, PaymentMethod.PAYPAL.groupId).contains(it.id) }
                            .map { transactionMethod ->
                                when (transactionMethod.id) {
                                    PaymentMethod.GOOGLE_PAY.groupId -> WalletMethod.GOOGLE_PAY
                                    else -> WalletMethod.NONE
                                }
                            },
                        transfers = backendMethods
                            .filter { transactionMethod ->
                                PaymentMethod.transfers()
                                    .map { paymentMethod -> paymentMethod.groupId }
                                    .contains(transactionMethod.id)
                            }
                            .map { TransactionMethod.fromDTO(it) }
                    )
            }
        }
    }
}