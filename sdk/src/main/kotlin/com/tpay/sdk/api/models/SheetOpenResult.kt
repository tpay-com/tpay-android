package com.tpay.sdk.api.models

/**
 * Class defining result of opening Tpay UI module
 */
sealed class SheetOpenResult {
    /**
     * Indicates that Tpay module opened successfully.
     */
    object Success : SheetOpenResult()

    /**
     * Indicates that configuration provided via TpayModule is invalid.
     */
    data class ConfigurationInvalid(val devMessage: String) : SheetOpenResult()

    /**
     * Indicates that there was a unexpected error while opening Tpay module.
     */
    data class UnexpectedError(val devErrorMessage: String?) : SheetOpenResult()
}
