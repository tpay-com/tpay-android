package com.tpay.sdk.api.webView

/**
 * Class responsible for storing web view configuration.
 * Keep in mind that Android web view normalizes urls in accordance to RFC 3986.
 * You have to pass normalized [successUrl] and [errorUrl] urls.
 *
 * @param [paymentUrl] url that will be displayed in the web view, Tpay backend
 * returns it if needed
 * @param [successUrl] url that Tpay frontend will redirect to when payment is completed
 * @param [errorUrl] url that Tpay frontend will redirect to when payment couldn't be completed
 */
data class WebViewConfiguration(
    val paymentUrl: String,
    val successUrl: String,
    val errorUrl: String
) {
    fun check(): WebViewCheckResult {
        if (paymentUrl.isBlank()) {
            return WebViewCheckResult.Invalid(WebViewCheckResult.PAYMENT_URL_BLANK)
        }
        if (successUrl.isBlank()) {
            return WebViewCheckResult.Invalid(WebViewCheckResult.SUCCESS_URL_BLANK)
        }
        if (errorUrl.isBlank()) {
            return WebViewCheckResult.Invalid(WebViewCheckResult.ERROR_URL_BLANK)
        }

        return WebViewCheckResult.Valid
    }
}
