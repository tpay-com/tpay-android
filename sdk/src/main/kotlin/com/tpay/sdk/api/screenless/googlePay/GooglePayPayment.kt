package com.tpay.sdk.api.screenless.googlePay

import android.util.Base64
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.*
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO

/**
 * Class responsible for creating Google Pay payment
 */
class GooglePayPayment private constructor(
    private val request: CreateTransactionWithChannelsDTO
) : Payment<CreateGooglePayTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreateGooglePayTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                val result = TransactionResponseValidator.validateGooglePay(response)

                longPollingConfig?.run {
                    if (result is CreateGooglePayTransactionResult.Created) {
                        longPolling.start(result.transactionId, this)
                    }
                }

                onResult(result)
            }, { e ->
                onResult(CreateGooglePayTransactionResult.Error(e.message))
            })
    }

    /**
     * Class responsible for building [GooglePayPayment].
     */
    class Builder : PaymentBuilder<GooglePayPayment>() {
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
         * Function adding Google Pay token to request
         */
        fun setGooglePayToken(token: String): Builder = apply {
            val encodedToken = Base64.encodeToString(token.encodeToByteArray(), Base64.NO_WRAP)
            transactionRequest.applyGooglePayToken(encodedToken)
        }

        override fun build(): GooglePayPayment {
            return GooglePayPayment(transactionRequest)
        }
    }
}