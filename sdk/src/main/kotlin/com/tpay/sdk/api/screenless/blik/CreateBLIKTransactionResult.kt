package com.tpay.sdk.api.screenless.blik


sealed class CreateBLIKTransactionResult {
    /**
     * Indicates that transaction was created.
     * It is advised to use long polling mechanism to observe transaction state.
     */
    data class Created(val transactionId: String) : CreateBLIKTransactionResult()

    /**
     * Indicates that transaction was created and paid.
     */
    data class CreatedAndPaid(val transactionId: String) : CreateBLIKTransactionResult()

    /**
     * Indicates that payer has the same BLIK alias registered in more than one bank app.
     * You need to display a list of [aliases] to user and then use [BLIKAmbiguousAliasPayment] to
     * continue the payment.
     */
    data class AmbiguousBlikAlias(val transactionId: String, val aliases: List<AmbiguousAlias>) : CreateBLIKTransactionResult()

    /**
     * Indicates that transaction was created but configured payment failed
     * due to expired data, incorrect data or banking problem.
     */
    data class ConfiguredPaymentFailed(val transactionId: String, val errorMessage: String?) : CreateBLIKTransactionResult()

    /**
     * Indicates that creating transaction failed due to an unexpected error (server or client side).
     * Transaction might be still available on the server if error was client side
     */
    data class Error(val errorMessage: String?, val transactionId: String? = null) : CreateBLIKTransactionResult()
}