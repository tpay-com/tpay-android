package com.tpay.sdk.server.dto.response

import androidx.annotation.Keep
import com.tpay.sdk.extensions.map
import com.tpay.sdk.server.dto.ResultDTO
import com.tpay.sdk.server.dto.parts.PaymentChannelDTO

@Keep
internal class GetChannelsResponseDTO(json: String) : ResultDTO(json) {
    val language: String = getString("language")
    val currency: String = getString("currency")
    val channels: List<PaymentChannelDTO> = getJSONArray("channels").map(::PaymentChannelDTO)
}