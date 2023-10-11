package com.tpay.sdk.internal


internal data class PaymentCoordinator(
    val paymentCreated: (String?) -> Unit,
    val paymentCompleted: (String?) -> Unit,
    val paymentCancelled: (String?) -> Unit,
    val moduleClosed: () -> Unit
)