package com.tpay.sdk.api.providers

import com.tpay.sdk.api.models.Language

/**
 * Interface defining provider for merchant details in different languages
 */
interface MerchantDetailsProvider {
    fun merchantDisplayName(language: Language): String
    fun merchantCity(language: Language): String?
    fun regulationsLink(language: Language): String
}
