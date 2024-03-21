package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class GroupDTO(json: String) : JSONObject(json) {
    val id: String = getString("id")
    val name: String = getString("name")
    val image: ImageDTO = ImageDTO(getJSONObject("image"))
}