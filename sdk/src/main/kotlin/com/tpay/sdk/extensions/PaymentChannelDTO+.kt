package com.tpay.sdk.extensions

import com.tpay.sdk.api.screenless.channelMethods.PaymentChannel
import com.tpay.sdk.internal.PaymentConstraintMapper
import com.tpay.sdk.server.dto.parts.PaymentChannelDTO

internal val PaymentChannelDTO.toModel: PaymentChannel
    get() = PaymentChannel(
        id = id,
        name = fullName,
        imageUrl = image.url,
        isAvailable = available,
        isOnline = onlinePayment,
        isInstantRedirectionAvailable = instantRedirection,
        groups = groups.map { dto -> dto.toModel },
        constraints = PaymentConstraintMapper.getConstraints(constraints)
    )
