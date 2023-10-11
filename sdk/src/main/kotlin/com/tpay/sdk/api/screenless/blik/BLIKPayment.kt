@file:Suppress("unused")
package com.tpay.sdk.api.screenless.blik

import com.tpay.sdk.api.models.BlikAlias
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.*
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO

/**
 * Class responsible for creating BLIK payment
 */
class BLIKPayment private constructor(
    private val request: CreateTransactionRequestDTO
) : Payment<CreateBLIKTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreateBLIKTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                val result = TransactionResponseValidator.validateBLIK(response)

                longPollingConfig?.run {
                    if (result is CreateBLIKTransactionResult.Created) {
                        longPolling.start(
                            transactionId = result.transactionId,
                            longPollingConfig = this,
                            isBlikPayment = true
                        )
                    }
                }

                onResult(result)
            }, { e ->
                onResult(CreateBLIKTransactionResult.Error(e.message))
            })
    }

    /**
     * Class responsible for building [BLIKPayment].
     */
    class Builder : PaymentBuilder<BLIKPayment>() {
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
         * Function adding 6-digit BLIK code to request.
         * This method overrides payment information set by [setBLIKAlias] and [setBLIKCodeAndRegisterAlias] methods.
         */
        fun setBLIKCode(code: String): Builder = apply {
            transactionRequest.applyBLIK(blikCode = code)
        }

        /**
         * Function adding 6-digit BLIK code and a [BlikAlias] to register after successful payment.
         * Use any alias. Using a registered alias will give user a chance to register it in a another bank.
         * This method overrides payment information set by [setBLIKCode] and [setBLIKAlias] methods.
         */
        fun setBLIKCodeAndRegisterAlias(code: String, blikAlias: BlikAlias) = apply {
            transactionRequest.applyBLIK(
                blikCode = code,
                aliasValue = blikAlias.value,
                aliasLabel = blikAlias.label
            )
        }

        /**
         * Function adding BLIK alias to request. Use only registered aliases.
         * This method overrides payment information set by [setBLIKCode] and [setBLIKCodeAndRegisterAlias] methods.
         */
        fun setBLIKAlias(blikAlias: BlikAlias): Builder = apply {
            transactionRequest.applyBLIK(aliasValue = blikAlias.value, aliasLabel = blikAlias.label)
        }

        override fun build(): BLIKPayment {
            return BLIKPayment(transactionRequest)
        }
    }
}