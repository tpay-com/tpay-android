package com.tpay.sdk.api.screenless.pekaoInstallment

import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.LongPollingConfig
import com.tpay.sdk.api.screenless.Notifications
import com.tpay.sdk.api.screenless.Payment
import com.tpay.sdk.api.screenless.PaymentBuilder
import com.tpay.sdk.api.screenless.PaymentDetails
import com.tpay.sdk.api.screenless.Redirects
import com.tpay.sdk.api.screenless.TransactionResponseValidator
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO

class PekaoInstallmentPayment private constructor(
    private val request: CreateTransactionWithChannelsDTO
) : Payment<CreatePekaoInstallmentTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreatePekaoInstallmentTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                val result = TransactionResponseValidator.validatePekaoInstallment(response)

                longPollingConfig?.run {
                    if (result is CreatePekaoInstallmentTransactionResult.Created) {
                        longPolling.start(result.transactionId, this)
                    }
                }

                onResult(result)
            }, { e ->
                onResult(CreatePekaoInstallmentTransactionResult.Error(e.message))
            })
    }

    class Builder : PaymentBuilder<PekaoInstallmentPayment>() {
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
         * Function adding channel id of Pekao installment to request
         */
        fun setChannelId(channelId: Int) = apply {
            transactionRequest.applyPekaoInstallment(channelId)
        }

        override fun build(): PekaoInstallmentPayment {
            return PekaoInstallmentPayment(transactionRequest)
        }
    }
}