package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import com.tpay.sdk.extensions.map
import org.json.JSONObject

@Keep
internal class PaymentChannelDTO(json: String) : JSONObject(json) {
    val id: String = getString("id")
    val name: String = getString("name")
    val fullName: String = getString("fullName")
    val image: ImageDTO = ImageDTO(getJSONObject("image"))
    val available: Boolean = getBoolean("available")
    val onlinePayment: Boolean = getBoolean("onlinePayment")
    val instantRedirection: Boolean = getBoolean("instantRedirection")
    val groups: List<GroupDTO> = getJSONArray("groups").map(::GroupDTO)
    val constraints: List<ConstraintDTO> = getJSONArray("constraints").map(::ConstraintDTO)
}