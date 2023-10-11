@file:Suppress("unused")

package com.tpay.sdk.server.dto

import androidx.annotation.Keep
import org.json.JSONObject


@Keep
internal abstract class ResultDTO(json: String) : JSONObject(json) {
    val result: String by lazy { getString("result") }
    val requestId: String by lazy { getString("requestId") }
}