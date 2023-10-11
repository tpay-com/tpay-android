package com.tpay.sdk.server.dto.response

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.ResultDTO

@Keep
internal class CardTokenizationResponseDTO(json: String) : ResultDTO(json) {
    var id: String? = optString("id")
    var url: String? = optString("url")
}