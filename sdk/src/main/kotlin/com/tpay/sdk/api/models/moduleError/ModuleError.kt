package com.tpay.sdk.api.models.moduleError

/**
 * Class responsible for defining module errors
 */
sealed class ModuleError : Error() {
    sealed class ConfigurationError(val devMessage: String) : ModuleError() {
        object SSLCertificateProviderNotProvided : ConfigurationError(SSL_CERTIFICATE_MISSING)
        object MerchantCredentialsNotProvided : ConfigurationError(MERCHANT_CREDENTIALS_MISSING)
        object MerchantDetailsProviderNotProvided : ConfigurationError(MERCHANT_DETAILS_MISSING)
        object MerchantIdNotProvided : ConfigurationError(MERCHANT_ID_MISSING)

        companion object {
            private const val SSL_CERTIFICATE_MISSING = "Please provide SSLCertificatesProvider via TpayModule."
            private const val MERCHANT_CREDENTIALS_MISSING = "Please provide Merchant via TpayModule."
            private const val MERCHANT_DETAILS_MISSING = "Please provide MerchantDetailsProvider via TpayModule."
            private const val MERCHANT_ID_MISSING = "Please provide a valid merchant id."
        }
    }
}
