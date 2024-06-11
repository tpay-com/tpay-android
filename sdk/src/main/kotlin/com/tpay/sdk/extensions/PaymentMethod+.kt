package com.tpay.sdk.extensions

import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.InstallmentPayment
import com.tpay.sdk.api.models.PaymentMethod

internal fun List<PaymentMethod>.containsWallet(wallet: DigitalWallet): Boolean {
    return filterIsInstance<PaymentMethod.DigitalWallets>()
        .firstOrNull()
        ?.contains(wallet) ?: false
}

internal fun List<PaymentMethod>.containsInstallment(installmentPayment: InstallmentPayment): Boolean {
    return filterIsInstance<PaymentMethod.InstallmentPayments>()
        .firstOrNull()
        ?.methods
        ?.contains(installmentPayment) ?: false
}

internal inline fun <reified T> List<PaymentMethod>.containsMethodOfType(): Boolean {
    return filterIsInstance<T>().isNotEmpty()
}