package com.tpay.sdk.api.models

/**
 * Class defining available payment methods
 */
sealed class PaymentMethod {
    object Blik : PaymentMethod()
    object Pbl : PaymentMethod()
    object Card : PaymentMethod()
    data class DigitalWallets(val wallets: List<DigitalWallet>) : PaymentMethod()

    companion object {
        val allMethods: List<PaymentMethod> = listOf(
            Blik, Pbl, Card, DigitalWallets(DigitalWallet.values().toList())
        )
    }
}

