package com.tpay.sdk.extensions

import java.util.*

internal val Calendar.month: Int
    get() = get(Calendar.MONTH) + 1

internal val Calendar.year: Int
    get() = get(Calendar.YEAR)