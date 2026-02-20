package com.tpay.sdk.api.tpayModule

import com.tpay.sdk.api.models.Compatibility
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.models.GooglePayConfiguration
import com.tpay.sdk.api.models.Language
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.providers.MerchantDetailsProvider
import com.tpay.sdk.api.providers.SSLCertificatesProvider
import com.tpay.sdk.internal.config.Configuration

/**
 * Class responsible for configuring Tpay module
 */
sealed class TpayModule {
    companion object {
        internal val configuration = Configuration()

        /**
         * Function responsible for configuring merchant information
         */
        fun configure(merchant: Merchant): Companion {
            configuration.merchant = merchant
            return this
        }

        /**
         * Function responsible for configuring environment
         */
        fun configure(environment: Environment): Companion {
            configuration.environment = environment
            return this
        }

        /**
         * Function responsible for configuring payment methods
         */
        fun configure(paymentMethods: List<PaymentMethod>): Companion {
            configuration.paymentMethods = paymentMethods.ifEmpty { PaymentMethod.allMethods }
            return this
        }

        /**
         * Function responsible for configuring ssl certificates
         */
        fun configure(sslCertificatesProvider: SSLCertificatesProvider): Companion {
            configuration.sslCertificatesProvider = sslCertificatesProvider
            return this
        }

        /**
         * Function responsible for configuring merchant details provider
         */
        fun configure(merchantDetailsProvider: MerchantDetailsProvider): Companion {
            configuration.merchantDetailsProvider = merchantDetailsProvider
            return this
        }

        /**
         * Function responsible for configuring the compatibility mode.
         * [Compatibility.NATIVE] is set by default.
         */
        fun configure(compatibility: Compatibility, sdkVersionName: String? = null): Companion {
            configuration.setCompatibility(compatibility, sdkVersionName)
            return this
        }

        /**
         * Function responsible for configuring Google Pay
         */
        fun configure(googlePayConfiguration: GooglePayConfiguration): Companion {
            configuration.googlePayConfiguration = googlePayConfiguration
            return this
        }

        /**
         * Function responsible for configuring languages available in Tpay UI module
         * @param [preferredLanguage] language that will be displayed
         * @param [supportedLanguages] languages that user will be able to use
         *
         * @throws IllegalArgumentException if supported language list is empty
         */
        fun configure(
            preferredLanguage: Language,
            supportedLanguages: List<Language> = Language.entries
        ): Companion {
            if(supportedLanguages.isEmpty()){
                throw IllegalArgumentException("List of supported languages cannot be empty")
            }

            configuration.preferredLanguage = preferredLanguage

            supportedLanguages
                .distinct()
                .let { languages ->
                    configuration.supportedLanguages = if (languages.contains(preferredLanguage)) {
                        languages
                    } else {
                        listOf(preferredLanguage) + languages
                    }
                }

            return this
        }
    }
}
