package com.tpay.sdk.internal

import androidx.lifecycle.SavedStateHandle
import com.tpay.sdk.api.addCard.Tokenization
import com.tpay.sdk.api.models.CertificatePinningConfiguration
import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.InstallmentPayment
import com.tpay.sdk.api.models.Language
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.models.transaction.Transaction
import com.tpay.sdk.api.providers.MerchantDetailsProvider
import com.tpay.sdk.api.providers.SSLCertificatesProvider
import com.tpay.sdk.extensions.containsMethodOfType
import com.tpay.sdk.extensions.getOrThrow
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.model.LocalizedString

internal class SheetViewModel(private val savedStateHandle: SavedStateHandle) : BaseViewModel() {
    var languageSelectedByUser = com.tpay.sdk.internal.Language.POLISH
        get() = savedStateHandle[LANGUAGE_SELECTED_BY_USER_KEY]
            ?: com.tpay.sdk.internal.Language.POLISH
        set(value) {
            savedStateHandle[LANGUAGE_SELECTED_BY_USER_KEY] = value
            field = value
        }

    init {
        if (configuration.merchant != null) saveConfigurationToState()
        if (configuration.merchant == null) readConfigurationFromState()
    }

    fun saveConfigurationToState() {
        configuration.run {
            savedStateHandle[AUTHORIZATION_KEY] = merchant?.authorization
            savedStateHandle[ENVIRONMENT_KEY] = environment
            savedStateHandle[PREFERRED_LANGUAGE_KEY] = preferredLanguage
            savedStateHandle[SUPPORTED_LANGUAGES_KEY] = supportedLanguages
            savedStateHandle[COMPATIBILITY_KEY] = compatibility
            savedStateHandle[GOOGLE_PAY_CONFIGURATION_KEY] = googlePayConfiguration
            savedStateHandle[PUBLIC_KEY_HASH_KEY] =
                sslCertificatesProvider?.apiConfiguration?.publicKeyHash
            savedStateHandle[MERCHANT_NAMES_KEY] = Language.values()
                .map { language ->
                    LocalizedString(
                        language,
                        merchantDetailsProvider?.merchantDisplayName(language) ?: ""
                    )
                }
            savedStateHandle[MERCHANT_CITIES_KEY] = Language.values()
                .map { language ->
                    LocalizedString(
                        language,
                        merchantDetailsProvider?.merchantCity(language) ?: ""
                    )
                }
            savedStateHandle[REGULATION_URLS_KEY] = Language.values()
                .map { language ->
                    LocalizedString(
                        language,
                        merchantDetailsProvider?.regulationsLink(language) ?: ""
                    )
                }
            savedStateHandle[CREDIT_CARD_SELECTED_KEY] =
                paymentMethods.containsMethodOfType<PaymentMethod.Card>()
            savedStateHandle[BLIK_SELECTED_KEY] =
                paymentMethods.containsMethodOfType<PaymentMethod.Blik>()
            savedStateHandle[TRANSFER_SELECTED_KEY] =
                paymentMethods.containsMethodOfType<PaymentMethod.Pbl>()
            savedStateHandle[DIGITAL_WALLETS_SELECTED_KEY] = paymentMethods
                .filterIsInstance<PaymentMethod.DigitalWallets>().firstOrNull()?.wallets
                ?: emptyList()
            savedStateHandle[INSTALLMENT_PAYMENTS_SELECTED_KEY] = paymentMethods
                .filterIsInstance<PaymentMethod.InstallmentPayments>().firstOrNull()?.methods
                ?: emptyList()
        }
    }

    fun readConfigurationFromState() {
        try {
            savedStateHandle.run {
                configuration.run {
                    merchant = Merchant(authorization = getOrThrow(AUTHORIZATION_KEY))
                    environment = getOrThrow(ENVIRONMENT_KEY)
                    preferredLanguage = getOrThrow(PREFERRED_LANGUAGE_KEY)
                    supportedLanguages = getOrThrow(SUPPORTED_LANGUAGES_KEY)
                    compatibility = getOrThrow(COMPATIBILITY_KEY)
                    googlePayConfiguration = get(GOOGLE_PAY_CONFIGURATION_KEY)
                    sslCertificatesProvider = object : SSLCertificatesProvider {
                        override var apiConfiguration: CertificatePinningConfiguration =
                            CertificatePinningConfiguration(getOrThrow(PUBLIC_KEY_HASH_KEY))
                    }
                    val merchantNames: List<LocalizedString> = getOrThrow(MERCHANT_NAMES_KEY)
                    val merchantCities: List<LocalizedString> = getOrThrow(MERCHANT_CITIES_KEY)
                    val regulationUrls: List<LocalizedString> = getOrThrow(REGULATION_URLS_KEY)
                    merchantDetailsProvider = object : MerchantDetailsProvider {
                        override fun merchantDisplayName(language: Language): String {
                            return merchantNames.firstOrNull { localized ->
                                localized.language == language
                            }?.value ?: ""
                        }

                        override fun merchantCity(language: Language): String {
                            return merchantCities.firstOrNull { localized ->
                                localized.language == language
                            }?.value ?: ""
                        }

                        override fun regulationsLink(language: Language): String {
                            return regulationUrls.firstOrNull { localized ->
                                localized.language == language
                            }?.value ?: ""
                        }
                    }
                    paymentMethods = mutableListOf<PaymentMethod>().apply {
                        if (getOrThrow(CREDIT_CARD_SELECTED_KEY)) {
                            add(PaymentMethod.Card)
                        }
                        if (getOrThrow(BLIK_SELECTED_KEY)) {
                            add(PaymentMethod.Blik)
                        }
                        if (getOrThrow(TRANSFER_SELECTED_KEY)) {
                            add(PaymentMethod.Pbl)
                        }
                        val wallets: List<DigitalWallet> =
                            getOrThrow(DIGITAL_WALLETS_SELECTED_KEY)
                        if (wallets.isNotEmpty()) {
                            add(PaymentMethod.DigitalWallets(wallets))
                        }
                        val installmentPayments: List<InstallmentPayment> =
                            getOrThrow(INSTALLMENT_PAYMENTS_SELECTED_KEY)
                        if (installmentPayments.isNotEmpty()) {
                            add(PaymentMethod.InstallmentPayments(installmentPayments))
                        }
                    }
                }

            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun saveRepositoryToState() = repository.run {
        selectedPaymentMethod.run {
            val selectedMethodKey = when (this) {
                is PaymentMethod.Card -> CARD
                is PaymentMethod.Blik -> BLIK
                is PaymentMethod.Pbl -> TRANSFER
                is PaymentMethod.DigitalWallets -> {
                    savedStateHandle[SELECTED_PAYMENT_METHOD_WALLETS_KEY] = wallets
                    DIGITAL_WALLETS
                }
                is PaymentMethod.InstallmentPayments -> {
                    savedStateHandle[SELECTED_PAYMENT_METHOD_INSTALLMENTS_KEY] = methods
                    INSTALLMENT_PAYMENTS
                }
                else -> null
            }
            savedStateHandle[SELECTED_PAYMENT_METHOD_KEY] = selectedMethodKey
        }
        savedStateHandle[CARD_TOKEN_TRANSACTION_KEY] = cardTokenTransaction
        try {
            savedStateHandle[TRANSACTION_KEY] = transaction
        } catch (_: Exception) {}
        try {
            savedStateHandle[TOKENIZATION_KEY] = tokenization
        } catch (_: Exception) {}
        savedStateHandle[TRANSACTION_ID_KEY] = transactionId
        savedStateHandle[TOKENIZATION_ID_KEY] = tokenizationId
        savedStateHandle[WEB_URL_KEY] = webUrl
        savedStateHandle[AVAILABLE_PAYMENT_METHODS_KEY] = availablePaymentMethods
    }

    fun readRepositoryFromState() = repository.run {
        val selectedMethodKey: String? = savedStateHandle[SELECTED_PAYMENT_METHOD_KEY]
        selectedPaymentMethod = selectedMethodKey?.run {
            when (this) {
                CARD -> PaymentMethod.Card
                BLIK -> PaymentMethod.Blik
                TRANSFER -> PaymentMethod.Pbl
                DIGITAL_WALLETS -> {
                    val wallets: List<DigitalWallet> =
                        savedStateHandle.getOrThrow(SELECTED_PAYMENT_METHOD_WALLETS_KEY)
                    PaymentMethod.DigitalWallets(wallets)
                }
                INSTALLMENT_PAYMENTS -> {
                    val installmentPayments: List<InstallmentPayment> =
                        savedStateHandle.getOrThrow(SELECTED_PAYMENT_METHOD_INSTALLMENTS_KEY)
                    PaymentMethod.InstallmentPayments(installmentPayments)
                }
                else -> null
            }
        }
        cardTokenTransaction = savedStateHandle[CARD_TOKEN_TRANSACTION_KEY]
        savedStateHandle.get<Transaction>(TRANSACTION_KEY)?.run { transaction = this }
        savedStateHandle.get<Tokenization>(TOKENIZATION_KEY)?.run { tokenization = this }
        transactionId = savedStateHandle[TRANSACTION_ID_KEY]
        tokenizationId = savedStateHandle[TOKENIZATION_ID_KEY]
        webUrl = savedStateHandle[WEB_URL_KEY]
        availablePaymentMethods = savedStateHandle[AVAILABLE_PAYMENT_METHODS_KEY]
    }

    companion object {
        private const val LANGUAGE_SELECTED_BY_USER_KEY = "LANGUAGE_SELECTED_BY_USER"

        // Repository
        private const val CARD = "CARD"
        private const val BLIK = "BLIK"
        private const val TRANSFER = "TRANSFER"
        private const val DIGITAL_WALLETS = "DIGITAL_WALLETS"
        private const val INSTALLMENT_PAYMENTS = "INSTALLMENT_PAYMENTS"
        private const val SELECTED_PAYMENT_METHOD_KEY = "SELECTED_PAYMENT_METHOD"
        private const val SELECTED_PAYMENT_METHOD_WALLETS_KEY = "SELECTED_PAYMENT_METHOD_WALLETS"
        private const val SELECTED_PAYMENT_METHOD_INSTALLMENTS_KEY =
            "SELECTED_PAYMENT_METHOD_INSTALLMENTS"
        private const val CARD_TOKEN_TRANSACTION_KEY = "CARD_TOKEN_TRANSACTION"
        private const val TRANSACTION_KEY = "TRANSACTION"
        private const val TOKENIZATION_KEY = "TOKENIZATION"
        private const val TRANSACTION_ID_KEY = "TRANSACTION_ID"
        private const val TOKENIZATION_ID_KEY = "TOKENIZATION_ID"
        private const val WEB_URL_KEY = "WEB_URL"
        private const val AVAILABLE_PAYMENT_METHODS_KEY = "AVAILABLE_PAYMENT_METHODS"

        // Configuration
        private const val AUTHORIZATION_KEY = "AUTHORIZATION"
        private const val ENVIRONMENT_KEY = "ENVIRONMENT"
        private const val PREFERRED_LANGUAGE_KEY = "PREFERRED_LANGUAGE"
        private const val SUPPORTED_LANGUAGES_KEY = "SUPPORTED_LANGUAGES"
        private const val COMPATIBILITY_KEY = "COMPATIBILITY"
        private const val GOOGLE_PAY_CONFIGURATION_KEY = "GOOGLE_PAY_CONFIGURATION"
        private const val PUBLIC_KEY_HASH_KEY = "PUBLIC_KEY_HASH"
        private const val MERCHANT_NAMES_KEY = "MERCHANT_NAMES"
        private const val MERCHANT_CITIES_KEY = "MERCHANT_CITIES"
        private const val REGULATION_URLS_KEY = "REGULATION_URLS"
        private const val CREDIT_CARD_SELECTED_KEY = "CREDIT_CARD_SELECTED"
        private const val BLIK_SELECTED_KEY = "BLIK_SELECTED"
        private const val TRANSFER_SELECTED_KEY = "TRANSFER_SELECTED"
        private const val DIGITAL_WALLETS_SELECTED_KEY = "DIGITAL_WALLETS_SELECTED"
        private const val INSTALLMENT_PAYMENTS_SELECTED_KEY = "INSTALLMENT_PAYMENTS_SELECTED"
    }
}