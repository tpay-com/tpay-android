package com.tpay.sdk.api.models.merchant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing information about merchant
 * @param [authorization] authorization including clientId and clientSecret
 */
data class Merchant(
    val authorization: Authorization
) {
    @Parcelize
    data class Authorization(
        val clientId: String,
        val clientSecret: String
    ) : Parcelable
}
