package com.tpay.sdk.internal.webView

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentWebViewBinding
import com.tpay.sdk.extensions.runDelayedOnMainThread
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.internal.base.BaseFragment


internal class WebViewFragment : BaseFragment(R.layout.fragment_web_view) {
    override val binding: FragmentWebViewBinding by viewBinding(FragmentWebViewBinding::bind)
    override val viewModel: WebViewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetFragment.run {
            isSheetHeaderVisible = false
            hideUserCard(withAnim = false)
            hideLanguageBtn(withAnim = false)
            runDelayedOnMainThread({
                setSheetFullscreen()
            }, if (savedInstanceState == null) 0 else OPEN_DELAY)
        }

        observeViewModelFields()
        enableCookies()
        setupWebView()
        viewModel.init()
    }

    private fun observeViewModelFields(){
        viewModel.isTransactionFinished.observe { isFinished ->
            if(isFinished) {
                sheetFragment.setSheetStandardHeight()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(){
        binding.root.run {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request ?: kotlin.run {
                        requireActivity().onBackPressed()
                        return false
                    }

                    return when (val url = request.url.toString()){
                        viewModel.getSuccessUrl() -> {
                            viewModel.onSuccess()
                            true
                        }
                        viewModel.getErrorUrl() -> {
                            viewModel.onError()
                            true
                        }
                        else -> {
                            url.endsWith(PDF_EXTENSION).also { isTpayDocument ->
                                if (isTpayDocument) {
                                    requireActivity()
                                        .startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        )
                                }
                            }
                        }
                    }
                }
            }

            viewModel.getUrl().run {
                if(this == null){
                    requireActivity().onBackPressed()
                } else {
                    loadUrl(this)
                }
            }
        }
    }

    private fun enableCookies(){
        CookieManager.getInstance().run {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.root, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
        sheetFragment.setSheetStandardHeight()
    }

    companion object {
        private const val OPEN_DELAY = 1000L
        private const val PDF_EXTENSION = ".pdf"
    }
}