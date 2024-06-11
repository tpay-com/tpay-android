package com.tpay.sdk.extensions

import com.tpay.sdk.api.webView.WebViewConfiguration
import org.json.JSONObject

private const val PAYMENT_URL = "paymentUrl"
private const val SUCCESS_URL = "successUrl"
private const val ERROR_URL = "errorUrl"

internal fun WebViewConfiguration.toJson(): String {
    return JSONObject().apply {
        put(PAYMENT_URL, paymentUrl)
        put(SUCCESS_URL, successUrl)
        put(ERROR_URL, errorUrl)
    }.toString()
}

internal fun webViewConfigurationFromJson(json: String): WebViewConfiguration = JSONObject(json).run {
    WebViewConfiguration(
        paymentUrl = getString(PAYMENT_URL),
        successUrl = getString(SUCCESS_URL),
        errorUrl = getString(ERROR_URL)
    )
}