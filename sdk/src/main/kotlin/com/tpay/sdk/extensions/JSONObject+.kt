package com.tpay.sdk.extensions

import org.json.JSONObject

fun JSONObject.actuallyOptString(key: String): String? {
    return optString(key).let { text ->
        if (text == "null" || text.isBlank()) null
        else text
    }
}