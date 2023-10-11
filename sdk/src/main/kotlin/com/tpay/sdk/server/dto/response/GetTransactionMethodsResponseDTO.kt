package com.tpay.sdk.server.dto.response

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.ResultDTO
import com.tpay.sdk.server.dto.parts.TransactionMethodsDTO


@Keep
internal class GetTransactionMethodsResponseDTO(json: String) : ResultDTO(json) {
    var transactionMethodsDTO: TransactionMethodsDTO = TransactionMethodsDTO(getJSONObject("groups").toString())
}