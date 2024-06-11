package com.tpay.sdk.internal.webViewModule

import javax.inject.Singleton

@Singleton
internal data class WebViewCoordinator(
    var onPaymentSuccess: () -> Unit = {},
    var onPaymentFailure: () -> Unit = {},
    var onWebViewClosed: () -> Unit = {}
)