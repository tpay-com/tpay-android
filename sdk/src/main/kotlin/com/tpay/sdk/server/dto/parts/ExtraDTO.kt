@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class ExtraDTO(json: String) : JSONObject(json) {
    var responseCode: String? = optString("responseCode")
    var responseDescription: String? = optString("responseDescription")
}