package com.tpay.sdk.api.screenless.payPo

import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.LongPollingConfig
import com.tpay.sdk.api.screenless.Notifications
import com.tpay.sdk.api.screenless.Payment
import com.tpay.sdk.api.screenless.PaymentBuilder
import com.tpay.sdk.api.screenless.PaymentDetails
import com.tpay.sdk.api.screenless.Redirects
import com.tpay.sdk.api.screenless.TransactionResponseValidator
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO
import com.tpay.sdk.server.dto.request.PayTransactionRequestDTO
import com.tpay.sdk.server.dto.response.CreateTransactionResponseDTO

/**
 * Class responsible for creating PayPo payment
 */
class PayPoPayment private constructor(
    private val request: CreateTransactionWithChannelsDTO
) : Payment<CreatePayPoTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreatePayPoTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                handleTransactionResponse(longPollingConfig, response, onResult)
            }, { e ->
                onResult(CreatePayPoTransactionResult.Error(e.message))
            })
    }


    fun continueTransaction(
        longPollingConfig: LongPollingConfig? = null,
        transactionId: String,
        onResult: (CreatePayPoTransactionResult) -> Unit
    ) {
        request.pay?.channelId?.let { channelId ->
            continueTransaction(transactionId, PayTransactionRequestDTO().apply {
                this.channelId = channelId
            })
                .observe({ response ->
                    handleTransactionResponse(longPollingConfig, response, onResult)
                }, { e ->
                    onResult(CreatePayPoTransactionResult.Error(e.message))
                })
        }
    }

    private fun handleTransactionResponse(
        longPollingConfig: LongPollingConfig?,
        response: CreateTransactionResponseDTO,
        onResult: (CreatePayPoTransactionResult) -> Unit
    ) {
        val result = TransactionResponseValidator.validatePayPo(response)

        longPollingConfig?.run {
            if (result is CreatePayPoTransactionResult.Created) {
                longPolling.start(result.transactionId, this)
            }
        }

        onResult(result)
    }

    class Builder : PaymentBuilder<PayPoPayment>() {
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

        override fun build(): PayPoPayment {
            return PayPoPayment(transactionRequest.applyPayPo())
        }
    }
}