package com.tpay.sdk.internal

import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.internal.model.TransactionMethods
import com.tpay.sdk.internal.paymentMethod.WalletMethod

internal object TransactionMethodsUtil {
    fun getAvailableMethods(
        apiTransactionMethods: TransactionMethods,
        configurationPaymentMethods: List<PaymentMethod>
    ): TransactionMethods {
        return TransactionMethods(
            cardPaymentAvailable = apiTransactionMethods.cardPaymentAvailable && configurationPaymentMethods.firstOrNull { it is PaymentMethod.Card } != null,
            blikPaymentAvailable = apiTransactionMethods.blikPaymentAvailable && configurationPaymentMethods.firstOrNull { it is PaymentMethod.Blik } != null,
            transfers = if (configurationPaymentMethods.firstOrNull { it is PaymentMethod.Pbl } != null) apiTransactionMethods.transfers else emptyList(),
            wallets = (configurationPaymentMethods.firstOrNull { it is PaymentMethod.DigitalWallets } as? PaymentMethod.DigitalWallets)
                ?.wallets
                ?.map { digitalWallet ->
                    when {
                        digitalWallet == DigitalWallet.GOOGLE_PAY && apiTransactionMethods.wallets.contains(
                            WalletMethod.GOOGLE_PAY) -> WalletMethod.GOOGLE_PAY
                        else -> WalletMethod.NONE
                    }
                }
                ?.filter { it != WalletMethod.NONE } ?: emptyList()
        )
    }
}