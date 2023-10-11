package com.tpay.sdk.server.dto.request

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.parts.PayerDTO
import com.tpay.sdk.server.dto.parts.PayerUrlDTO
import org.json.JSONObject

@Keep
internal class CardTokenizationRequestDTO : JSONObject() {
    var payer: PayerDTO? = null
        set(value) {
            put("payer", value)
            field = value
        }

    var callbackUrl: String? = null
        set(value) {
            put("callbackUrl", value)
            field = value
        }

    var card: String? = null
        set(value) {
            put("card", value)
            field = value
        }

    var redirects: PayerUrlDTO? = null
        set(value) {
            put("redirectUrl", value)
            field = value
        }
}