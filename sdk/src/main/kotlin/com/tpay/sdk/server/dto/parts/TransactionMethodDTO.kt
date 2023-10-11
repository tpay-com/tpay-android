package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class TransactionMethodDTO(json: String) : JSONObject(json) {
    var id: String = getString("id")
    var name: String = getString("name")
    var img: String = getString("img")
    var availablePaymentChannels: List<String> =
        getJSONArray("availablePaymentChannels").let { jsonArray ->
            val channels = arrayListOf<String>()
            for (index in 0 until jsonArray.length()){
                jsonArray.getString(index)?.let { value -> channels.add(value) }
            }
            channels
        }
    var mainChannel: String = getString("mainChannel")
}