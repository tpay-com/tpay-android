package com.tpay.sdk.internal.model

import com.tpay.sdk.api.screenless.channelMethods.ChannelMethod
import com.tpay.sdk.api.screenless.channelMethods.WalletMethod

internal data class AvailableMethods(
    val creditCard: ChannelMethod? = null,
    val blik: ChannelMethod? = null,
    val wallets: List<WalletMethod> = emptyList(),
    val transfers: List<MethodWithImage> = emptyList(),
    val pekaoInstallments: List<MethodWithImage> = emptyList()
)
