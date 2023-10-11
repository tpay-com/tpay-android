package com.tpay.sdk.api.payment


interface PaymentDelegate {
    /**
     * Called when payment was created on Tpay server
     *
     * @param transactionId - id of created transaction
     */
    fun onPaymentCreated(transactionId: String?)

    /**
     * Called when user paid for transaction
     *
     * @param transactionId - id of completed transaction
     */
    fun onPaymentCompleted(transactionId: String?)

    /**
     * Called when user cancelled payment or payment failed
     *
     * @param transactionId - id of cancelled transaction
     */
    fun onPaymentCancelled(transactionId: String?)

    /**
     * Called when module was closed
     * (CardTokenPayment cannot be closed without creating a transaction)
     */
    fun onModuleClosed()
}