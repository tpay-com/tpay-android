package com.tpay.sdk.extensions

import com.tpay.sdk.api.screenless.channelMethods.PaymentGroup
import com.tpay.sdk.server.dto.parts.GroupDTO

internal val GroupDTO.toModel: PaymentGroup
    get() = PaymentGroup(
        id = id,
        name = name,
        imageUrl = image.url
    )