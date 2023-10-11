package com.tpay.sdk.api.models

import com.tpay.sdk.api.models.moduleError.ModuleError

/**
 * Class defining result of Tpay configuration check
 */
sealed class ConfigurationCheckResult {
    /**
     * Indicates that configuration provided via TpayModule is valid
     */
    object Valid : ConfigurationCheckResult()

    /**
     * Indicates that configuration provided via TpayModule is invalid
     */
    data class Invalid(val error: ModuleError.ConfigurationError) : ConfigurationCheckResult()
}
