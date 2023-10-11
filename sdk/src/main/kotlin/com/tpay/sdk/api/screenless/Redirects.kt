package com.tpay.sdk.api.screenless


/**
 * Class responsible for storing payment redirect data
 *
 * @param [successUrl] user will be redirected to this url if payment is successful
 * @param [errorUrl] user will be redirected to this url if payment is not successful
 */
data class Redirects(
    val successUrl: String,
    val errorUrl: String
)
