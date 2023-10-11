@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class PayerResponseDTO(json: String) : JSONObject(json) {
    var payerId: String? = optString("payerId")
    var email: String? = optString("email")
    var name: String? = optString("name")
    var phone: String? = optString("phone")
    var address: String? = optString("address")
    var postalCode: String? = optString("postalCode")
    var city: String? = optString("city")
    var country: String? = optString("country")
}