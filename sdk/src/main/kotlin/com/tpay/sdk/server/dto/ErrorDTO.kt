@file:Suppress("unused")

package com.tpay.sdk.server.dto

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class ErrorDTO(json: String) : JSONObject(json) {
    var errorCode: String? = optString("errorCode")
    var errorMessage: String? = optString("errorMessage")
    var fieldName: String? = optString("fieldName")
    var devMessage: String? = optString("devMessage")
    var docUrl: String? = optString("docUrl")
}

internal fun List<ErrorDTO>.readMessages(): String {
    return fold(""){ acc, dto ->
        acc + dto.errorMessage + ". "
    }
}