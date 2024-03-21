package com.tpay.sdk.api.models

/**
 * Class providing compatibility options
 */
sealed class Compatibility {
    /**
     * Sets compatibility options for native Android development
     */
    object Native : Compatibility()

    /**
     * Sets compatibility options for Flutter plugin development
     */
    object Flutter : Compatibility()
}
