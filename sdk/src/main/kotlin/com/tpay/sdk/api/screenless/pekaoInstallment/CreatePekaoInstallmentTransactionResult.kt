package com.tpay.sdk.api.screenless.pekaoInstallment

sealed class CreatePekaoInstallmentTransactionResult {
    /**
     * Indicates that transaction was created. Display [paymentUrl] to finish payment.
     * It is advised to use long polling mechanism to observe transaction state.
     */
    data class Created(val transactionId: String, val paymentUrl: String) : CreatePekaoInstallmentTransactionResult()

    /**
     * Indicates that creating transaction failed due to an unexpected error (server or client side).
     * Transaction might be still available on the server if error was client side
     */
    data class Error(val devErrorMessage: String?, val transactionId: String? = null) : CreatePekaoInstallmentTransactionResult()
}
