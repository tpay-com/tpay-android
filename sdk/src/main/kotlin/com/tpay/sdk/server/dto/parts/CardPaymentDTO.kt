package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class CardPaymentDTO : JSONObject() {
    var card: String? = null
        set(value) {
            put("card", value)
            field = value
        }

    var token: String? = null
        set(value) {
            put("token", value)
            field = value
        }

    var save: Int? = null
        set(value) {
            put("save", value)
            field = value
        }

    var rocText: String? = null
        set(value) {
            put("rocText", value)
            field = value
        }
}