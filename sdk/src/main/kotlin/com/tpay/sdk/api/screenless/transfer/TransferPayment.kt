@file:Suppress("unused")
package com.tpay.sdk.api.screenless.transfer

import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.*
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO

/**
 * Class responsible for creating transfer payment.
 */
class TransferPayment private constructor(
    private val request: CreateTransactionRequestDTO
) : Payment<CreateTransferTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreateTransferTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                val result = TransactionResponseValidator.validateTransfer(response)

                longPollingConfig?.run {
                    if (result is CreateTransferTransactionResult.Created) {
                        longPolling.start(result.transactionId, this)
                    }
                }

                onResult(result)
            }, { e ->
                onResult(CreateTransferTransactionResult.Error(e.message))
            })
    }

    /**
     * Class responsible for building [TransferPayment].
     */
    class Builder : PaymentBuilder<TransferPayment>() {
        /**
         * Function adding payer information to payment using [Payer] class.
         */
        fun setPayer(payer: Payer): Builder = apply {
            payer(payer)
        }

        /**
         * Function adding redirect and notification urls using [Redirects]
         * and [Notifications] classes to payment.
         */
        fun setCallbacks(
            redirects: Redirects? = null,
            notifications: Notifications? = null
        ): Builder = apply {
            callbacks(redirects, notifications)
        }

        /**
         * Function adding payment information like amount or description
         * using [PaymentDetails] class.
         */
        fun setPaymentDetails(paymentDetails: PaymentDetails): Builder = apply {
            paymentDetails(paymentDetails)
        }

        /**
         * Function setting group id of bank to request
         */
        fun setGroupId(groupId: Int): Builder = apply {
            transactionRequest.applyTransfer(groupId)
        }

        override fun build(): TransferPayment {
            return TransferPayment(transactionRequest)
        }
    }
}