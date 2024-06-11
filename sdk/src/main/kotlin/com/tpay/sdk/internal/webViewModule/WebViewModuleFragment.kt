@file:SuppressLint("SetJavaScriptEnabled")

package com.tpay.sdk.internal.webViewModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tpay.sdk.R
import com.tpay.sdk.api.webView.WebViewConfiguration
import com.tpay.sdk.databinding.FragmentWebViewModuleBinding
import com.tpay.sdk.extensions.runDelayedOnMainThread
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.extensions.webViewConfigurationFromJson
import com.tpay.sdk.internal.SheetFragment

internal class WebViewModuleFragment : Fragment(R.layout.fragment_web_view_module) {
    private val binding: FragmentWebViewModuleBinding by viewBinding(FragmentWebViewModuleBinding::bind)
    private val webView: WebView by lazy { binding.root }
    private val webViewConfiguration: WebViewConfiguration by webViewConfiguration()
    private val viewModel: WebViewModuleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareSheet(isFirstRun = savedInstanceState == null)
        setupWebView()
        enableCookies()

        webView.loadUrl(webViewConfiguration.paymentUrl)
    }

    private fun setupWebView() = webView.run {
        settings.run {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request ?: kotlin.run {
                    return false
                }

                val action = viewModel.onRedirectUrl(request.url.toString(), webViewConfiguration)
                if (action is RedirectAction.OpenIntent) {
                    requireActivity().startActivity(action.intent)
                }

                return action is RedirectAction.BlockLoading || action is RedirectAction.OpenIntent
            }
        }
    }

    private fun enableCookies() = CookieManager.getInstance().run {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(webView, true)
    }

    private fun webViewConfiguration(): Lazy<WebViewConfiguration> = lazy {
        arguments?.getString(WEB_VIEW_DATA_KEY)?.run(::webViewConfigurationFromJson)
            ?: throw IllegalStateException(WEB_VIEW_DATA_NOT_FOUND)
    }

    private fun prepareSheet(isFirstRun: Boolean) = (parentFragment as? SheetFragment)?.run {
        isSheetHeaderVisible = false
        hideUserCard(withAnim = false)
        hideLanguageBtn(withAnim = false)
        runDelayedOnMainThread({
            setSheetFullscreen()
        }, if (isFirstRun) 0 else OPEN_DELAY)
    }

    companion object {
        const val WEB_VIEW_DATA_KEY = "com.tpay.sdk.web_view_data_key"
        private const val WEB_VIEW_DATA_NOT_FOUND = "Web view configuration data not found!"
        private const val OPEN_DELAY = 1000L
    }
}
