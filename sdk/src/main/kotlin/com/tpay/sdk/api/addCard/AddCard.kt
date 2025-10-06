package com.tpay.sdk.api.addCard

import android.app.Activity
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.api.models.ConfigurationCheckResult
import com.tpay.sdk.api.models.Presentable
import com.tpay.sdk.api.models.SheetOpenResult
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.internal.AddCardCoordinator
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.ScreenOrientationUtil
import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.addCard.AddCardFragment
import com.tpay.sdk.internal.config.Configuration
import javax.inject.Inject

/**
 * Entrypoint for credit card tokenization using Tpay UI module
 */
sealed class AddCard {
    class Sheet(
        private val tokenization: Tokenization,
        private val activity: Activity,
        private val supportFragmentManager: FragmentManager
    ) : AddCard(), Presentable {
        private var sheetFragment = SheetFragment.with(SheetType.TOKENIZATION)

        @Inject
        private lateinit var repository: Repository

        @Inject
        internal lateinit var configuration: Configuration

        @Inject
        internal lateinit var addCardCoordinator: AddCardCoordinator

        init {
            injectFields()
            supportFragmentManager.getFragmentOrNull<SheetFragment>()?.let { fragment ->
                sheetFragment = fragment
            } ?: saveArgs()
        }

        private fun saveArgs() {
            repository.tokenization = tokenization
        }

        override fun present(): SheetOpenResult {
            val configurationResult = configuration.checkAddCardConfiguration()
            return if (configurationResult is ConfigurationCheckResult.Invalid) {
                SheetOpenResult.ConfigurationInvalid(configurationResult.error.devMessage)
            } else {
                try {
                    if (!sheetFragment.isAdded) {
                        sheetFragment = SheetFragment.with(SheetType.TOKENIZATION)
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

        /**
         * Function responsible for adding Tokenization observer
         */
        fun addObserver(addCardDelegate: AddCardDelegate) {
            addCardCoordinator.run {
                addCardSuccess = addCardDelegate::onAddCardSuccess
                addCardFailure = addCardDelegate::onAddCardFailure
                moduleClosed = addCardDelegate::onModuleClosed
            }
        }

        /**
         * Function responsible for removing Tokenization observer
         */
        fun removeObserver() {
            addCardCoordinator.run {
                addCardSuccess = { }
                addCardFailure = { }
                moduleClosed = { }
            }
        }

        override fun onBackPressed() {
            when (sheetFragment.childFragmentManager.fragments.lastOrNull()) {
                is AddCardFragment -> sheetFragment.closeSheet()
                else -> Unit
            }
        }

        companion object {
            /**
             * Function responsible for restoring the AddCardDelegate callback
             * after a process death occurred.
             *
             * If the AddCard.Sheet was open during process death, system will automatically
             * open it again after user comes back to the app. In this case you need to call this method
             * to restore the callback and receive tokenization information.
             */
            fun restore(supportFragmentManager: FragmentManager, addCardDelegate: AddCardDelegate) {
                supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()
                    ?.addTokenizationDelegate(addCardDelegate)
            }

            /**
             * Function responsible for checking if the AddCard.Sheet is currently open
             */
            fun isOpen(supportFragmentManager: FragmentManager): Boolean {
                return supportFragmentManager
                    .getFragmentOrNull<SheetFragment>()?.sheetType == SheetType.TOKENIZATION
            }
        }
    }
}