@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.ErrorDTO
import org.json.JSONObject

@Keep
internal class PaymentsDTO(json: String) : JSONObject(json) {
    var groupId: Int? = optInt("groupId")
    var status: String? = optString("status")
    var method: String? = optString("method")
    var amountPaid: Double? = optDouble("amountPaid")
    var date: CreationRealizationDateDTO? = optJSONObject("date")?.toString()?.let {
        if(it.isNotBlank()){
            CreationRealizationDateDTO(it)
        } else {
            null
        }
    }
    var bankDataPayer: PayerResponseDTO? = optJSONObject("bankDataPayer")?.toString()?.let {
        if (it.isNotBlank()){
            PayerResponseDTO(it)
        } else {
            null
        }
    }
    var attempts: List<AttemptDTO>? = optJSONArray("attempts")?.let { array ->
        val temp = mutableListOf<AttemptDTO>()
        for (index in 0 until array.length()){
            temp.add(AttemptDTO(array.get(index).toString()))
        }
        temp
    }
    var extra: ExtraDTO? = optJSONObject("extra")?.toString()?.let {
        if(it.isNotBlank()){
            ExtraDTO(it)
        } else {
            null
        }
    }
    var errors: List<ErrorDTO>? = optJSONArray("errors")?.let { array ->
        (0 until array.length()).map { ErrorDTO(array[it].toString()) }
    }
    val alternatives: List<AmbiguousBlikAliasDTO>? = optJSONArray("alternatives")?.let { array ->
        (0 until array.length()).map { AmbiguousBlikAliasDTO(array[it].toString()) }
    }
}