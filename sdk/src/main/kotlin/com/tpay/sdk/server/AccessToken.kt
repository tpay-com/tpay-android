package com.tpay.sdk.server

data class AccessToken(
    val token: String,
    val validForSeconds: Int
) {
    private val validUntil = currentTimeSeconds + validForSeconds

    val isValid: Boolean
        get() = currentTimeSeconds < (validUntil - VALIDITY_OFFSET_SECONDS)

    private val currentTimeSeconds
        get() = System.currentTimeMillis() / 1000

    companion object {
        private const val VALIDITY_OFFSET_SECONDS = 100
    }
}