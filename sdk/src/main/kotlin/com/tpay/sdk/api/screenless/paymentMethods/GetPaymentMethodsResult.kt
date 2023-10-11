package com.tpay.sdk.api.screenless.paymentMethods

import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.internal.model.TransactionMethod

sealed class GetPaymentMethodsResult {
    /**
     * Indicates that request was successful
     * @param [isCreditCardPaymentAvailable] if true credit card payments are available
     * @param [isBLIKPaymentAvailable] if true BLIK payments are available
     * @param [availableTransferMethods] list of available transfer methods
     * @param [availableDigitalWallets] list of available digital wallets
     */
    data class Success(
        val isCreditCardPaymentAvailable: Boolean,
        val isBLIKPaymentAvailable: Boolean,
        val availableTransferMethods: List<TransactionMethod>,
        val availableDigitalWallets: List<DigitalWallet>
    ) : GetPaymentMethodsResult()

    /**
     * Indicates that request failed.
     */
    data class Error(val devErrorMessage: String?) : GetPaymentMethodsResult()
}
