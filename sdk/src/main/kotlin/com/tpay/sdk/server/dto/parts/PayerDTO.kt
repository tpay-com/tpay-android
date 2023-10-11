package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class PayerDTO(json: String? = null) : JSONObject() {
    var email: String? = null
        set(value) {
            field = value
            put("email", value)
        }
    var name: String? = null
        set(value) {
            field = value
            put("name", value)
        }

    var phone: String? = null
        set(value) {
            field = value
            put("phone", value?.ifBlank { null })
        }

    var address: String? = null
        set(value) {
            field = value
            put("address", value?.ifBlank { null })
        }

    var city: String? = null
        set(value) {
            field = value
            put("city", value?.ifBlank { null })
        }

    var code: String? = null
        set(value) {
            field = value
            put("code", value?.ifBlank { null })
        }

    var country: String? = null
        set(value) {
            field = value
            put("country", value?.ifBlank { null })
        }

    init {
        json?.let {
            email = optString("email")
            name = optString("name")
            phone = optString("phone")
            address = optString("address")
            city = optString("city")
            code = optString("postalCode")
        }
    }
}