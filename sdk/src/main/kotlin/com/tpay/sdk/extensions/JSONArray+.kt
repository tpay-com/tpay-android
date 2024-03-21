package com.tpay.sdk.extensions

import org.json.JSONArray

internal fun <T> JSONArray.map(transform: (String) -> T): List<T> = mutableListOf<T>().apply {
    for (index in (0 until length())) {
        add(transform(this@map.get(index).toString()))
    }
}
