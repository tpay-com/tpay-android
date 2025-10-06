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
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.internal.ActivityResultHandler
import com.tpay.sdk.internal.PaymentCoordinator
import com.tpay.sdk.internal.PaymentCoordinators
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.ScreenOrientationUtil
import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.payerData.PayerDataFragment
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
            when (sheetFragment.childFragmentManager.fragments.lastOrNull()) {
                is PayerDataFragment -> sheetFragment.closeSheet()
                is PaymentMethodFragment -> sheetFragment.navigation.onBackPressed()
                else -> Unit
            }
        }

        companion object {
            /**
             * Function responsible for restoring the PaymentDelegate callback
             * after a process death occurred.
             *
             * If the Payment.Sheet was open during process death, system will automatically
             * open it again after user comes back to the app. In this case you need to call this method
             * to restore the callback and receive transaction information.
             */
            fun restore(supportFragmentManager: FragmentManager, paymentDelegate: PaymentDelegate) {
                supportFragmentManager.getFragmentOrNull<SheetFragment>()?.addPaymentDelegate(
                    sheetType = SheetType.PAYMENT,
                    paymentDelegate = paymentDelegate
                )
            }

            /**
             * Function responsible for checking if the Payment.Sheet is currently open
             */
            fun isOpen(supportFragmentManager: FragmentManager): Boolean {
                return supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()?.sheetType == SheetType.PAYMENT
            }

            /**
             * Function responsible for passing activity result data to Payment sheet
             * after a process death occurred.
             *
             * It is important to call this method if you use Google Pay inside Tpay module
             * as the credit card data is sent via onActivityResult(...) method in your activity.
             */
            fun onActivityResult(
                supportFragmentManager: FragmentManager,
                requestCode: Int,
                resultCode: Int,
                data: Intent?
            ) {
                supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()
                    ?.activityResultFromRestore(requestCode, resultCode, data)
            }

            /**
             * Function responsible for passing a back press event to Payment.Sheet.
             *
             * Use this method if you lost access to the Payment.Sheet instance.
             */
            fun onBackPress(supportFragmentManager: FragmentManager) {
                supportFragmentManager.getFragmentOrNull<SheetFragment>()?.run {
                    if (childFragmentManager.fragments.lastOrNull() is PaymentMethodFragment) {
                        navigation.onBackPressed()
                    }
                }
            }
        }
    }
}
