package com.tpay.sdk.api.screenless.blik

/**
 * Class responsible for storing information about ambiguous BLIK alias
 * @param [name] alias display name, show this as a label to user
 * @param [code] alias code used to select bank app
 */
data class AmbiguousAlias(
    val name: String,
    val code: String
)
