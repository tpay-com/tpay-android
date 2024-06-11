package com.tpay.sdk.internal.webView

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

internal sealed class WebUrl(open val url: String): Parcelable {
    @Parcelize
    internal data class Payment(override val url: String) : WebUrl(url)
    @Parcelize
    internal data class Tokenization(override val url: String) : WebUrl(url)
}
