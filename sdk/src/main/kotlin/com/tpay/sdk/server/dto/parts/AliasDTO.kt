@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class AliasDTO : JSONObject() {
    var value: String? = null
        set(value) {
            put("value", value)
            field = value
        }

    var type: String? = null
        set(value) {
            put("type", value)
            field = value
        }

    var label: String? = null
        set(value) {
            put("label", value)
            field = value
        }

    var key: String? = null
        set(value) {
            put("key", value)
            field = value
        }
}