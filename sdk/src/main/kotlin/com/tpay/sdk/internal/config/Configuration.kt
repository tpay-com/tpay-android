package com.tpay.sdk.internal.config

import com.tpay.sdk.api.models.Compatibility
import com.tpay.sdk.api.models.ConfigurationCheckResult
import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.models.GooglePayConfiguration
import com.tpay.sdk.api.models.Language
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.models.moduleError.ModuleError
import com.tpay.sdk.api.providers.MerchantDetailsProvider
import com.tpay.sdk.api.providers.SSLCertificatesProvider
import javax.inject.Singleton

@Singleton
internal class Configuration {
    // Save values to state handle
    var merchant: Merchant? = null
    var paymentMethods: List<PaymentMethod> = PaymentMethod.allMethods
    var environment: Environment = Environment.PRODUCTION
    var sslCertificatesProvider: SSLCertificatesProvider? = null
    var merchantDetailsProvider: MerchantDetailsProvider? = null
    var preferredLanguage: Language = Language.PL
    var supportedLanguages: List<Language> = Language.values().toList()
    var compatibility: Compatibility = Compatibility.NATIVE
    var googlePayConfiguration: GooglePayConfiguration? = null

    fun checkPaymentConfiguration(): ConfigurationCheckResult {
        val isGooglePayInMethods = paymentMethods
            .filterIsInstance<PaymentMethod.DigitalWallets>()
            .firstOrNull()?.wallets?.contains(DigitalWallet.GOOGLE_PAY) == true

        return when {
            sslCertificatesProvider == null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.SSLCertificateProviderNotProvided)
            merchant == null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.MerchantCredentialsNotProvided)
            isGooglePayInMethods && (googlePayConfiguration == null || googlePayConfiguration?.merchantId?.isBlank() == true) -> {
                ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.GooglePayNotConfigured)
            }
            merchantDetailsProvider == null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.MerchantDetailsProviderNotProvided)
            else -> ConfigurationCheckResult.Valid
        }
    }

    fun checkAddCardConfiguration() = when {
        sslCertificatesProvider == null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.SSLCertificateProviderNotProvided)
        merchant == null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.MerchantCredentialsNotProvided)
        else -> ConfigurationCheckResult.Valid
    }

    fun checkTokenPaymentConfiguration() = when (merchant) {
        null -> ConfigurationCheckResult.Invalid(ModuleError.ConfigurationError.MerchantCredentialsNotProvided)
        else -> ConfigurationCheckResult.Valid
    }
}