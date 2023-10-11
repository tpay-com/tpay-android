package com.tpay.sdk.internal.webView

internal sealed class WebUrl(val url: String) {
    internal class Payment(url: String) : WebUrl(url)
    internal class Tokenization(url: String) : WebUrl(url)
}
