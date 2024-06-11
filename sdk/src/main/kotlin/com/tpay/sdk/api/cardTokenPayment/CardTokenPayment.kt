package com.tpay.sdk.api.cardTokenPayment

import android.app.Activity
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.api.models.ConfigurationCheckResult
import com.tpay.sdk.api.models.ObservablePayment
import com.tpay.sdk.api.models.Presentable
import com.tpay.sdk.api.models.SheetOpenResult
import com.tpay.sdk.api.payment.PaymentDelegate
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.internal.*
import com.tpay.sdk.internal.PaymentCoordinator
import com.tpay.sdk.internal.PaymentCoordinators
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.config.Configuration
import javax.inject.Inject

/**
 * Entrypoint for credit card token payment using Tpay UI module
 */
sealed class CardTokenPayment {
    class Sheet(
        private val transaction: CardTokenTransaction,
        private val activity: Activity,
        private val supportFragmentManager: FragmentManager
    ) : CardTokenPayment(), Presentable, ObservablePayment {
        private var sheetFragment = SheetFragment.with(SheetType.TOKEN_PAYMENT)

        @Inject
        private lateinit var repository: Repository

        @Inject
        private lateinit var paymentCoordinators: PaymentCoordinators

        @Inject
        private lateinit var configuration: Configuration

        init {
            injectFields()
            supportFragmentManager.getFragmentOrNull<SheetFragment>()?.let { fragment ->
                sheetFragment = fragment
            } ?: saveArgs()
        }

        private fun saveArgs() {
            repository.cardTokenTransaction = transaction
        }

        override fun present(): SheetOpenResult {
            val configurationResult = configuration.checkTokenPaymentConfiguration()
            return if (configurationResult is ConfigurationCheckResult.Invalid) {
                SheetOpenResult.ConfigurationInvalid(configurationResult.error.devMessage)
            } else {
                try {
                    if (!sheetFragment.isAdded) {
                        sheetFragment = SheetFragment.with(SheetType.TOKEN_PAYMENT)
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
                SheetType.TOKEN_PAYMENT,
                PaymentCoordinator(
                    paymentCreated = paymentDelegate::onPaymentCreated,
                    paymentCompleted = paymentDelegate::onPaymentCompleted,
                    paymentCancelled = paymentDelegate::onPaymentCancelled,
                    moduleClosed = paymentDelegate::onModuleClosed
                )
            )
        }

        override fun removeObserver() {
            paymentCoordinators.remove(SheetType.TOKEN_PAYMENT)
        }

        override fun onBackPressed() {}

        companion object {
            /**
             * Function responsible for restoring the PaymentDelegate callback
             * after a process death occurred.
             *
             * If the CardTokenPayment.Sheet was open during process death, system will automatically
             * open it again after user comes back to the app. In this case you need to call this method
             * to restore the callback and receive transaction information.
             */
            fun restore(supportFragmentManager: FragmentManager, paymentDelegate: PaymentDelegate) {
                supportFragmentManager.getFragmentOrNull<SheetFragment>()?.addPaymentDelegate(
                    sheetType = SheetType.TOKEN_PAYMENT,
                    paymentDelegate = paymentDelegate
                )
            }

            /**
             * Function responsible for checking if the CardTokenPayment.Sheet is currently open
             */
            fun isOpen(supportFragmentManager: FragmentManager): Boolean {
                return supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()?.sheetType == SheetType.TOKEN_PAYMENT
            }
        }
    }
}
