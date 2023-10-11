package com.tpay.sdk.api.models.transaction

/**
 * Class defining quantity of recurring payments
 */
sealed class Quantity {
    object Indefinite : Quantity()
    data class Specified(val value: Int) : Quantity()
}
