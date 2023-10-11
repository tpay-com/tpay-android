package com.tpay.sdk.api.providers

import com.tpay.sdk.api.models.CertificatePinningConfiguration

/**
 * Interface defining provider for SSL certificates
 */
interface SSLCertificatesProvider {
    var apiConfiguration: CertificatePinningConfiguration
}
