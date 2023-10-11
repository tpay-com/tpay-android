package com.tpay.sdk.api.models

import com.tpay.sdk.api.payment.PaymentDelegate

interface ObservablePayment {
    /**
     * Function responsible for adding payment observer
     */
    fun addObserver(paymentDelegate: PaymentDelegate)

    /**
     * Function responsible for removing observer
     */
    fun removeObserver()
}