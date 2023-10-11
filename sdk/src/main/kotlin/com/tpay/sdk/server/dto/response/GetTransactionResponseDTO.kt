@file:Suppress("unused")

package com.tpay.sdk.server.dto.response

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.ResultDTO
import com.tpay.sdk.server.dto.parts.CreationRealizationDateDTO
import com.tpay.sdk.server.dto.parts.LockDTO
import com.tpay.sdk.server.dto.parts.PaymentsDTO


@Keep
internal class GetTransactionResponseDTO(json: String) : ResultDTO(json) {
    var title: String? = optString("title")
    var posId: String? = optString("posId")
    var status: String? = optString("status")
    var date: CreationRealizationDateDTO? = optJSONObject("date")?.toString()?.let {
        if(it.isNotBlank()){
            CreationRealizationDateDTO(it)
        } else {
            null
        }
    }
    var amount: Double? = optDouble("amount")
    var currency: String? = optString("currency")
    var description: String? = optString("description")
    var hiddenDescription: String? = optString("hiddenDescription")
    var lock: LockDTO? = optJSONObject("lock")?.toString()?.let {
        if(it.isNotBlank()){
            LockDTO(it)
        } else {
            null
        }
    }
    var payments: PaymentsDTO? = optJSONObject("payments")?.toString()?.let {
        if(it.isNotBlank()){
            PaymentsDTO(it)
        } else {
            null
        }
    }

    val isBlikError = payments?.attempts?.run {
        if (isEmpty()) return@run false
        val failedAttempt = firstOrNull { attempt ->
            attempt.paymentErrorCode != null
        } ?: return@run false

        failedAttempt.paymentErrorCode != null
    } ?: false
}