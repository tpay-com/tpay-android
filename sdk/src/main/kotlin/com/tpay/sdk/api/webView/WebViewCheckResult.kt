package com.tpay.sdk.api.webView

sealed class WebViewCheckResult {
    /**
     * Indicates that web view configuration is valid
     */
    object Valid : WebViewCheckResult()

    /**
     * Indicates that web view configuration is invalid
     */
    data class Invalid(val message: String) : WebViewCheckResult()

    companion object {
        const val PAYMENT_URL_BLANK = "Payment url can not be blank."
        const val SUCCESS_URL_BLANK = "Success url can not be blank."
        const val ERROR_URL_BLANK = "Error url can not be blank."
    }
}