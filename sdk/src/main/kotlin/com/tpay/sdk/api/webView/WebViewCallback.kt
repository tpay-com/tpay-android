package com.tpay.sdk.api.webView

interface WebViewCallback {
    /**
     * Called when web view detects that payment was completed
     */
    fun onPaymentSuccess()

    /**
     * Called when web view detects that payment couldn't be completed
     */
    fun onPaymentFailure()

    /**
     * Called always when web view is being closed, this includes events:
     * - user clicked the system back press button, without finishing the payment
     * - payment was successful, [onPaymentSuccess] method will also be called
     * - payment ended with an error, [onPaymentFailure] method will also be called
     */
    fun onWebViewClosed()
}