package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class TransactionMethodsDTO(json: String): JSONObject(json) {
    var transactionMethodDTOS: List<TransactionMethodDTO> = emptyList()

    init {
        val keys = keys()
        val methods = mutableListOf<TransactionMethodDTO>()
        while (keys.hasNext()){
            methods.add(TransactionMethodDTO(get(keys.next()).toString()))
        }
        transactionMethodDTOS = methods
    }
}