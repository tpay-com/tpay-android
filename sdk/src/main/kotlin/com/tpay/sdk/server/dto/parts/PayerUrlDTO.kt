package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class PayerUrlDTO : JSONObject() {
    var success: String? = null
        set(value) {
            put("success", value)
            field = value
        }
    var error: String? = null
        set(value) {
            put("error", value)
            field = value
        }
}