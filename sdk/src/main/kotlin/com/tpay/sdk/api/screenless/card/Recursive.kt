package com.tpay.sdk.api.screenless.card

import com.tpay.sdk.api.models.transaction.Frequency
import com.tpay.sdk.api.models.transaction.Quantity
import java.util.*

/**
 * Class responsible for storing information about recurring payments
 */
data class Recursive(
    val frequency: Frequency,
    val quantity: Quantity,
    val expirationDate: Date
)
