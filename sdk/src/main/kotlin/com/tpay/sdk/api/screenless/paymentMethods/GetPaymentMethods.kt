package com.tpay.sdk.api.screenless.paymentMethods

import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.TransactionMethodsUtil
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.model.TransactionMethods
import com.tpay.sdk.internal.paymentMethod.WalletMethod
import javax.inject.Inject

/**
 * Class responsible for getting available payment methods.
 * It takes a common part of payment methods from server and
 * payment methods configured with TpayModule.
 */
class GetPaymentMethods {

    @Inject
    private lateinit var repository: Repository

    @Inject
    private lateinit var configuration: Configuration

    init {
        injectFields()
        val authorization = configuration.merchant?.authorization ?: throw IllegalStateException(MERCHANT_AUTHORIZATION_MISSING)
        repository.setAuth(authorization, configuration.environment)
    }

    /**
     * Function responsible for executing the request.
     */
    fun execute(onResult: (GetPaymentMethodsResult) -> Unit) {
        repository
            .getAvailablePaymentMethods()
            .observe({ response ->
                try {
                    val transactionMethods = TransactionMethodsUtil.getAvailableMethods(
                        apiTransactionMethods = TransactionMethods.fromDTO(response),
                        configurationPaymentMethods = configuration.paymentMethods
                    )

                    onResult(
                        GetPaymentMethodsResult.Success(
                            isCreditCardPaymentAvailable = transactionMethods.cardPaymentAvailable,
                            isBLIKPaymentAvailable = transactionMethods.blikPaymentAvailable,
                            availableTransferMethods = transactionMethods.transfers,
                            availableDigitalWallets = transactionMethods
                                .wallets
                                .let { walletMethods ->
                                    mutableListOf<DigitalWallet>().apply {
                                        if (walletMethods.contains(WalletMethod.GOOGLE_PAY)) add(DigitalWallet.GOOGLE_PAY)
                                    }
                                }
                        )
                    )
                } catch (exception: Exception) {
                    onResult(GetPaymentMethodsResult.Error(exception.message))
                }
            }, { e ->
                onResult(GetPaymentMethodsResult.Error(e.message))
            })
    }

    companion object {
        private const val MERCHANT_AUTHORIZATION_MISSING = "Please provide merchant authorization via TpayModule."
    }
}