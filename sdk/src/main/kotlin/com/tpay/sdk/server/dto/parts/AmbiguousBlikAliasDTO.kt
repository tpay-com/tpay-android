package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
class AmbiguousBlikAliasDTO(json: String) : JSONObject(json) {
    val name: String = getString("applicationName")
    val code: String = getString("applicationCode")
}