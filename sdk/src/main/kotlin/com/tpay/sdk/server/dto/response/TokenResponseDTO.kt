@file:Suppress("unused")

package com.tpay.sdk.server.dto.response

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class TokenResponseDTO(json: String) : JSONObject(json) {
    var accessToken: String? = optString("access_token")
    var clientId: String? = optString("client_id")
    var expiresIn: Int? = optInt("expires_in")
    var issuedAt: Int? = optInt("issued_at")
    var scope: String? = optString("scope")
    var tokenType: String? = optString("token_type")
}