@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class LockDTO(json: String) : JSONObject(json) {
    var type: String? = optString("type")
    var status: String? = optString("status")
    var amount: Double? = optDouble("amount")
    var amountCollected: Double? = optDouble("amountCollected")
}