package com.tpay.sdk.api.webView

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.api.models.Presentable
import com.tpay.sdk.api.models.SheetOpenResult
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.extensions.toJson
import com.tpay.sdk.internal.ScreenOrientationUtil
import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.webViewModule.WebViewCoordinator
import com.tpay.sdk.internal.webViewModule.WebViewModuleFragment
import javax.inject.Inject

/**
 * Entrypoint for web view
 */
class WebView {
    class Sheet(
        private val webViewConfiguration: WebViewConfiguration,
        private val activity: Activity,
        private val supportFragmentManager: FragmentManager
    ) : Presentable {
        @Inject
        private lateinit var webViewCoordinator: WebViewCoordinator

        init {
            injectFields()
        }

        private val sheetFragment: SheetFragment
            get() = supportFragmentManager.getFragmentOrNull<SheetFragment>()
                ?: SheetFragment.with(
                    SheetType.WEB_VIEW,
                    bundleOf(WebViewModuleFragment.WEB_VIEW_DATA_KEY to webViewConfiguration.toJson())
                )

        override fun present(): SheetOpenResult = webViewConfiguration.check().run {
            when (this) {
                is WebViewCheckResult.Invalid -> SheetOpenResult.ConfigurationInvalid(message)
                else -> {
                    try {
                        sheetFragment.run {
                            if (!isAdded) {
                                supportFragmentManager
                                    .beginTransaction()
                                    .replace(android.R.id.content, this)
                                    .commit()
                            }
                        }
                        ScreenOrientationUtil.lock(activity)
                        SheetOpenResult.Success
                    } catch (exception: Exception) {
                        SheetOpenResult.UnexpectedError(exception.message)
                    }
                }
            }
        }

        fun setCallback(callback: WebViewCallback) {
            webViewCoordinator.run {
                onPaymentSuccess = {
                    callback.onPaymentSuccess()
                    sheetFragment.closeSheet()
                }
                onPaymentFailure = {
                    callback.onPaymentFailure()
                    sheetFragment.closeSheet()
                }
                onWebViewClosed = callback::onWebViewClosed
            }
        }

        fun removeCallback() {
            webViewCoordinator.run {
                onPaymentSuccess = {}
                onPaymentFailure = {}
                onWebViewClosed = {}
            }
        }

        override fun onBackPressed() {
            sheetFragment.closeSheet()
        }

        companion object {
            /**
             * Function responsible for restoring the WebViewCallback callback
             * after a process death occurred.
             *
             * If the WebView.Sheet was open during process death, system will automatically
             * open it again after user comes back to the app. In this case you need to call this method
             * to restore the callback and receive web view status.
             */
            fun restore(supportFragmentManager: FragmentManager, webViewCallback: WebViewCallback) {
                supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()?.addWebViewCallback(webViewCallback)
            }

            /**
             * Function responsible for checking if the WebView.Sheet is currently open
             */
            fun isOpen(supportFragmentManager: FragmentManager): Boolean {
                return supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()?.sheetType == SheetType.WEB_VIEW
            }

            /**
             * Function responsible for passing a back press event to WebView.Sheet.
             *
             * Use this method if you lost access to the WebView.Sheet instance.
             */
            fun onBackPress(supportFragmentManager: FragmentManager) {
                supportFragmentManager.getFragmentOrNull<SheetFragment>()?.closeSheet()
            }
        }
    }
}