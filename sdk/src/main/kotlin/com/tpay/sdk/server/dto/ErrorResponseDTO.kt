@file:Suppress("unused")

package com.tpay.sdk.server.dto

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class ErrorResponseDTO(json: String) : JSONObject(json) {
    var result: String? = optString("result")
    var requestId: String? = optString("requestId")
    private var errors: List<ErrorDTO>? = optJSONArray("errors")?.let { array ->
        val tempErrors = mutableListOf<ErrorDTO>()
        for (index in 0 until array.length()){
            tempErrors.add(ErrorDTO(array[index].toString()))
        }
        tempErrors
    }

    fun readErrorMessages(): String {
        return errors?.fold(""){ acc, errorDTO ->
            acc + errorDTO.errorMessage + " / "
        } ?: "?"
    }
}