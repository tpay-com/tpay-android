@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal class RecursivePaymentDTO : JSONObject() {
    var recursiveId: String? = null
        set(value) {
            put("recursiveId", value)
            field = value
        }

    var period: Int? = null
        set(value) {
            put("period", value)
            field = value
        }

    var quantity: Int? = null
        set(value) {
            put("quantity", value)
            field = value
        }

    var type: Int? = null
        set(value) {
            put("type", value)
            field = value
        }

    var expirationDate: String? = null
        set(value) {
            put("expirationDate", value)
            field = value
        }
}