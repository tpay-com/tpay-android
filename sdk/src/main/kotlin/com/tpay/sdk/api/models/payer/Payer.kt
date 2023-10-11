package com.tpay.sdk.api.models.payer

/**
 * Class responsible for storing payer information.
 */
data class Payer(
    var name: String,
    var email: String,
    var phone: String?,
    var address: Address?
) {
    data class Address(
        var address: String?,
        var city: String?,
        var countryCode: String?,
        var postalCode: String?
    )
}
