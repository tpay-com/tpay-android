package com.tpay.sdk.api.models.merchant

/**
 * Class responsible for storing information about merchant
 * @param [authorization] authorization including clientId and clientSecret
 */
data class Merchant(
    val authorization: Authorization
) {
    data class Authorization(
        val clientId: String,
        val clientSecret: String
    )
}
