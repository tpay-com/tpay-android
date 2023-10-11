@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class CreationRealizationDateDTO(json: String) : JSONObject(json) {
    var creation: String? = optString("creation")
    var realization: String? = optString("realization")
}