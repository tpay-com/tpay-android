@file:Suppress("unused")

package com.tpay.sdk.server.dto.parts

import androidx.annotation.Keep
import com.tpay.sdk.extensions.actuallyOptString
import org.json.JSONObject


@Keep
internal class AttemptDTO(json: String) : JSONObject(json) {
    var date: String? = optString("date")
    var paymentErrorCode: String? = actuallyOptString("paymentErrorCode")
}