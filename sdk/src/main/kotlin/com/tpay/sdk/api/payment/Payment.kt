package com.tpay.sdk.api.payment

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.api.models.ActivityResultListener
import com.tpay.sdk.api.models.ConfigurationCheckResult
import com.tpay.sdk.api.models.ObservablePayment
import com.tpay.sdk.api.models.Presentable
import com.tpay.sdk.api.models.SheetOpenResult
import com.tpay.sdk.api.models.transaction.Transaction
import com.tpay.sdk.cache.DirectoryManager
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.internal.*
import com.tpay.sdk.internal.PaymentCoordinator
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.paymentMethod.PaymentMethodFragment
import javax.inject.Inject

/**
 * Entrypoint for standard payment using Tpay UI module
 */
sealed class Payment {
    class Sheet(
        private val transaction: Transaction,
        private val activity: Activity,
        private val supportFragmentManager: FragmentManager,
    ) : Payment(), Presentable, ObservablePayment, ActivityResultListener {
        private var sheetFragment = SheetFragment.with(SheetType.PAYMENT)

        @Inject
        private lateinit var repository: Repository

        @Inject
        internal lateinit var paymentCoordinators: PaymentCoordinators

        @Inject
        internal lateinit var configuration: Configuration

        @Inject
        internal lateinit var directoryManager: DirectoryManager

        @Inject
        internal lateinit var activityResultHandler: ActivityResultHandler

        init {
            injectFields()
            supportFragmentManager.getFragmentOrNull<SheetFragment>()?.let { fragment ->
                sheetFragment = fragment
            } ?: saveArgs()
        }

        private fun saveArgs() {
            repository.transaction = transaction
        }

        override fun present(): SheetOpenResult {
            val configurationResult = configuration.checkPaymentConfiguration()
            return if (configurationResult is ConfigurationCheckResult.Invalid) {
                SheetOpenResult.ConfigurationInvalid(configurationResult.error.devMessage)
            } else {
                try {
                    directoryManager.init(activity)
                    Language.fromConfiguration(configuration.supportedLanguages)
                    if (!sheetFragment.isAdded) {
                        sheetFragment = SheetFragment.with(SheetType.PAYMENT)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(android.R.id.content, sheetFragment)
                            .commit()
                        ScreenOrientationUtil.lock(activity)
                    }
                    SheetOpenResult.Success
                } catch (exception: Exception) {
                    SheetOpenResult.UnexpectedError(exception.message)
                }
            }
        }

        override fun addObserver(paymentDelegate: PaymentDelegate) {
            paymentCoordinators.add(
                SheetType.PAYMENT,
                PaymentCoordinator(
                    paymentCreated = paymentDelegate::onPaymentCreated,
                    paymentCompleted = paymentDelegate::onPaymentCompleted,
                    paymentCancelled = paymentDelegate::onPaymentCancelled,
                    moduleClosed = paymentDelegate::onModuleClosed
                )
            )
        }

        override fun removeObserver() {
            paymentCoordinators.remove(SheetType.PAYMENT)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            activityResultHandler.onResult.value = Triple(requestCode, resultCode, data)
        }

        override fun onBackPressed() {
            sheetFragment.run {
                if (childFragmentManager.fragments.lastOrNull() is PaymentMethodFragment) {
                    navigation.onBackPressed()
                }
            }
        }
    }
}
