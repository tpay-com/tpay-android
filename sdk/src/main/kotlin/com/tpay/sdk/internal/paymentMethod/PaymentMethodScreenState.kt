package com.tpay.sdk.internal.paymentMethod

internal enum class PaymentMethodScreenState {
    CARD,
    CARD_ONE_CLICK,
    BLIK,
    BLIK_ONE_CLICK,
    BLIK_ONE_CLICK_CODE,
    BLIK_AMBIGUOUS,
    WALLET,
    TRANSFER
}