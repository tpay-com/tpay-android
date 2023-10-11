package com.tpay.sdk.api.screenless

import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO
import javax.inject.Inject


abstract class PaymentBuilder<T : Payment<*>> {
    internal val transactionRequest = CreateTransactionRequestDTO()

    @Inject
    private lateinit var configuration: Configuration

    init {
        injectFields()
    }

    protected val publicKey: String
        get() = configuration
            .sslCertificatesProvider
            ?.apiConfiguration
            ?.publicKeyHash ?: throw IllegalStateException(NO_PUBLIC_KEY_ERROR_MESSAGE)

    protected fun payer(payer: Payer){
        transactionRequest.applyPayer(payer)
    }

    protected fun callbacks(redirects: Redirects?, notifications: Notifications?){
        transactionRequest.applyCallbacks(
            successUrl = redirects?.successUrl,
            errorUrl = redirects?.errorUrl,
            notificationUrl = notifications?.notificationUrl,
            notificationEmail = notifications?.notificationEmail
        )
    }

    protected fun paymentDetails(paymentDetails: PaymentDetails){
        paymentDetails.run {
            transactionRequest.applyPaymentDetails(
                amount, description, hiddenDescription, language?.name?.lowercase()
            )
        }
    }

    abstract fun build(): T

    companion object {
        private const val NO_PUBLIC_KEY_ERROR_MESSAGE = "Configure public key using TpayModule before using this payment."
    }
}