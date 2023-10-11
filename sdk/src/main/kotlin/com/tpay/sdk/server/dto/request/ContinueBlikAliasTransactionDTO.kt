package com.tpay.sdk.server.dto.request

import com.tpay.sdk.api.models.BlikAlias
import com.tpay.sdk.api.screenless.blik.AmbiguousAlias
import com.tpay.sdk.server.dto.parts.AliasDTO
import com.tpay.sdk.server.dto.parts.BLIKPaymentDTO

internal class ContinueBlikAliasTransactionDTO(
    private val blikAlias: BlikAlias,
    private val ambiguousAlias: AmbiguousAlias
) : PayTransactionRequestDTO() {
    init {
        groupId = CreateTransactionRequestDTO.BLIK_GROUP_ID
        blikPaymentData = BLIKPaymentDTO().apply {
            aliases = AliasDTO().apply {
                type = CreateTransactionRequestDTO.BLIK_ALIAS_TYPE
                value = blikAlias.value
                label = blikAlias.label
                key = ambiguousAlias.code
            }
        }
    }
}