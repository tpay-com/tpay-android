package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class NotificationDTO : JSONObject() {
    var url: String? = null
        set(value) {
            put("url", value)
            field = value
        }
    var email: String? = null
        set(value) {
            put("email", value)
            field = value
        }
}