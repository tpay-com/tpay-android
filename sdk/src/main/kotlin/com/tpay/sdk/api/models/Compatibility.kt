package com.tpay.sdk.api.models

/**
 * Enum providing compatibility options
 */
enum class Compatibility {
    /**
     * Sets compatibility options for native Android development
     */
    NATIVE,

    /**
     * Sets compatibility options for Flutter plugin development
     */
    FLUTTER,

    /**
     * Sets compatibility options for React Native module development
     */
    REACT_NATIVE
}
