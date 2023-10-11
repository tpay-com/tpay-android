package com.tpay.sdk.api.addCard

import android.app.Activity
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.api.models.ConfigurationCheckResult
import com.tpay.sdk.api.models.Presentable
import com.tpay.sdk.api.models.SheetOpenResult
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.getFragmentOrNull
import com.tpay.sdk.internal.*
import com.tpay.sdk.internal.AddCardCoordinator
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.SheetFragment
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
                    Language.fromConfiguration(configuration.supportedLanguages)
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

        override fun onBackPressed() {}
    }
}