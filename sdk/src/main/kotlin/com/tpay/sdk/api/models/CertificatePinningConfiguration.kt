package com.tpay.sdk.api.models

/**
 * Class responsible for storing information about certificates
 * @param [publicKeyHash] public key used to encrypt credit card data
 */
data class CertificatePinningConfiguration(val publicKeyHash: String) {
    internal val pinnedDomain: String = "api.tpay.com"
}

