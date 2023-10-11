@file:Suppress("unused")

package com.tpay.sdk.server.dto.request

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.parts.BLIKPaymentDTO
import com.tpay.sdk.server.dto.parts.CardPaymentDTO
import com.tpay.sdk.server.dto.parts.RecursivePaymentDTO
import org.json.JSONObject


@Keep
internal open class PayTransactionRequestDTO : JSONObject() {
    var groupId: Int? = null
        set(value) {
            put("groupId", value)
            field = value
        }

    var method: String? = null
        set(value) {
            put("method",  value)
            field = value
        }

    var blikPaymentData: BLIKPaymentDTO? = null
        set(value) {
            put("blikPaymentData", value)
            field = value
        }

    var cardPaymentData: CardPaymentDTO? = null
        set(value) {
            put("cardPaymentData", value)
            field = value
        }

    var recursive: RecursivePaymentDTO? = null
        set(value) {
            put("recursive", value)
            field = value
        }

    var googlePayPaymentData: String? = null
        set(value) {
            put("googlePayPaymentData", value)
            field = value
        }

    var cof: String? = null
        set(value) {
            put("cof", value)
            field = value
        }
}