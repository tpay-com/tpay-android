package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class CallbacksDTO : JSONObject() {
    var payerUrls: PayerUrlDTO? = null
        set(value) {
            put("payerUrls", value)
            field = value
        }
    var notification: NotificationDTO? = null
        set(value) {
            put("notification", value)
            field = value
        }
}