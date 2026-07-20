@file:Suppress("unused")
package com.tpay.sdk.api.screenless.card

import com.tpay.sdk.api.PayCardEncryptor
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.*
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO
import com.tpay.sdk.server.dto.request.PayCardRequest
import com.tpay.sdk.server.dto.response.CreateTransactionResponseDTO
import java.text.SimpleDateFormat
import java.util.*

/**
 * Class responsible for creating credit card payment.
 */
class CreditCardPayment private constructor(
    private val request: CreateTransactionWithChannelsDTO
) : Payment<CreateCreditCardTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreateCreditCardTransactionResult) -> Unit
    ) {
        makeTransaction(request)
            .observe({ response ->
                handleTransactionResponse(longPollingConfig, response, onResult)
            }, { e ->
                onResult(CreateCreditCardTransactionResult.Error(errorMessage = e.message))
            })
    }

    fun continueTransaction(
        transactionId: String,
        longPollingConfig: LongPollingConfig? = null,
        onResult: (CreateCreditCardTransactionResult) -> Unit
    ) {
        request.pay?.let {payRequest ->
            val channelId = payRequest.channelId
            val cardPayment = payRequest.cardPaymentData
            if(channelId != null && cardPayment != null) {
                continueTransaction(transactionId, PayCardRequest(cardPayment, channelId))
                    .observe({ response ->
                        handleTransactionResponse(longPollingConfig, response, onResult)
                    }, { e ->
                        onResult(CreateCreditCardTransactionResult.Error(errorMessage = e.message))
                    })
            }
        }
    }

    private fun handleTransactionResponse(
        longPollingConfig: LongPollingConfig?,
        response: CreateTransactionResponseDTO,
        onResult: (CreateCreditCardTransactionResult) -> Unit
    ) {
        val result = TransactionResponseValidator.validateCreditCard(response)

        longPollingConfig?.run {
            if (result is CreateCreditCardTransactionResult.Created) {
                longPolling.start(result.transactionId, this)
            }
        }

        onResult(result)
    }

    /**
     * Class responsible for building [CreditCardPayment].
     */
    class Builder : PaymentBuilder<CreditCardPayment>() {
        private var recursive: Recursive? = null

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
         * Function responsible for configuring recurring credit card payments.
         */
        fun setRecursive(recursive: Recursive): Builder = apply {
            this.recursive = recursive
        }

        /**
         * Function responsible for setting generated credit card token used in
         * one click payments.
         */
        fun setCreditCardToken(token: String): Builder = apply {
            transactionRequest.applyPayCard(cardToken = token)
        }

        /**
         * Function responsible for setting credit card information.
         * @param [creditCard] credit card data
         * @param [domain] merchant domain name
         * @param [saveCard] if true, credit card token will be generated and sent to
         * notification url defined in [Notifications]
         * @param [rocText] custom identifier for settlement
         */
        fun setCreditCard(
            creditCard: CreditCard,
            domain: String,
            saveCard: Boolean = false,
            rocText: String? = null
        ): Builder = apply {
            val encrypted = PayCardEncryptor(publicKey).encrypt(
                cardNumber = creditCard.cardNumber,
                expirationDate = creditCard.expirationDate,
                cvv = creditCard.cvv,
                domain = domain
            )

            transactionRequest.applyPayCard(
                encryptedCardData = encrypted,
                saveCard = saveCard,
                roc = rocText
            )
        }

        override fun build(): CreditCardPayment {
            recursive?.run {
                transactionRequest.applyRecursive(
                    frequency,
                    quantity,
                    SimpleDateFormat(RECURSIVE_DATE_PATTERN, Locale.getDefault()).format(expirationDate)
                )
            }

            return CreditCardPayment(transactionRequest)
        }

        companion object {
            private const val RECURSIVE_DATE_PATTERN = "yyyy-MM-dd"
        }
    }
}