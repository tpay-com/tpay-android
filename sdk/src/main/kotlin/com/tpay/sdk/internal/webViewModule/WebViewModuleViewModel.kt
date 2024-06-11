package com.tpay.sdk.internal.webViewModule

import android.content.Intent
import android.net.Uri
import com.tpay.sdk.api.webView.WebViewConfiguration
import com.tpay.sdk.internal.base.BaseViewModel

internal class WebViewModuleViewModel : BaseViewModel() {
    fun onRedirectUrl(url: String, webViewConfiguration: WebViewConfiguration): RedirectAction =
        webViewConfiguration.run {
            when (url) {
                successUrl -> {
                    webViewCoordinator.onPaymentSuccess()
                    RedirectAction.BlockLoading
                }

                errorUrl -> {
                    webViewCoordinator.onPaymentFailure()
                    RedirectAction.BlockLoading
                }

                else -> {
                    val isDocument = url.endsWith(PDF_EXTENSION)
                    if (isDocument) {
                        RedirectAction.OpenIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } else {
                        RedirectAction.AllowLoading
                    }
                }
            }
        }

    companion object {
        private const val PDF_EXTENSION = ".pdf"
    }
}

internal sealed class RedirectAction {
    object BlockLoading : RedirectAction()
    object AllowLoading : RedirectAction()
    data class OpenIntent(val intent: Intent) : RedirectAction()
}
