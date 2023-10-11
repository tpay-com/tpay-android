package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class BLIKPaymentDTO : JSONObject() {
    var blikToken: String? = null
        set(value) {
            put("blikToken", value)
            field = value
        }

    var aliases: AliasDTO? = null
        set(value) {
            put("aliases", value)
            field = value
        }

    var type: Int? = null
        set(value) {
            put("type", value)
            field = value
        }
}