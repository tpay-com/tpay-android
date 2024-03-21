package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class ConstraintDTO(json: String) : JSONObject(json) {
    val field: String = getString("field")
    val type: String = getString("type")
    val value: String = getString("value")
}