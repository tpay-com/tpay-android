package com.tpay.sdk.server.dto.request

import androidx.annotation.Keep
import com.tpay.sdk.server.dto.parts.BLIKPaymentDTO
import com.tpay.sdk.server.dto.parts.CardPaymentDTO
import com.tpay.sdk.server.dto.parts.RecursivePaymentDTO
import org.json.JSONObject

@Keep
internal class PayWithRedirectionRequestDTO : JSONObject() {
    var channelId: Int? = null
        set(value) {
            put(CHANNEL_ID, value)
            field = value
        }

    var method: String? = null
        set(value) {
            put(METHOD, value)
            field = value
        }

    var blikPaymentData: BLIKPaymentDTO? = null
        set(value) {
            put(BLIK_PAYMENT_DATA, value)
            field = value
        }

    var cardPaymentData: CardPaymentDTO? = null
        set(value) {
            put(CARD_PAYMENT_DATA, value)
            field = value
        }

    var recursive: RecursivePaymentDTO? = null
        set(value) {
            put(RECURSIVE, value)
            field = value
        }

    var googlePayPaymentData: String? = null
        set(value) {
            put(GOOGLE_PAY_PAYMENT_DATA, value)
            field = value
        }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val METHOD = "method"
        private const val BLIK_PAYMENT_DATA = "blikPaymentData"
        private const val CARD_PAYMENT_DATA = "cardPaymentData"
        private const val RECURSIVE = "recursive"
        private const val GOOGLE_PAY_PAYMENT_DATA = "googlePayPaymentData"
    }
}