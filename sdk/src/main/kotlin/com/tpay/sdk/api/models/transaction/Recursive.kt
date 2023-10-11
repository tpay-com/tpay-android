package com.tpay.sdk.api.models.transaction

import java.util.*

interface Recursive {
    val frequency: Frequency
    val quantity: Quantity
    val expirationDate: Date
}
