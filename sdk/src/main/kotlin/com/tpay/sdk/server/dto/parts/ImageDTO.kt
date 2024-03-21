package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
class ImageDTO(json: JSONObject) {
    val url: String = json.getString("url")
}