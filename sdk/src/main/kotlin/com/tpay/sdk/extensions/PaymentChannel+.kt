package com.tpay.sdk.extensions

import com.tpay.sdk.api.screenless.channelMethods.PaymentChannel
import com.tpay.sdk.api.screenless.channelMethods.PaymentConstraint

internal fun List<PaymentChannel>.removeChannelsWithEmptyGroups(): List<PaymentChannel> {
    return filter { channel -> channel.groups.isNotEmpty() }
}

internal fun List<PaymentChannel>.ignoreChannels(channels: List<String>): List<PaymentChannel> {
    return filter { channel -> !channels.contains(channel.id) }
}

internal fun List<PaymentChannel>.channelWithGroupId(groupId: String): PaymentChannel? {
    return associate { channel -> channel.groups.firstOrNull()?.id to channel }[groupId]
}

internal fun List<PaymentChannel>.channelsWithGroupId(groupId: String): List<PaymentChannel> {
    return filter { channel -> channel.groups.firstOrNull()?.id == groupId }
}

internal fun List<PaymentChannel>.channelsWithout(otherChannels: List<PaymentChannel?>): List<PaymentChannel> {
    return filter { channel -> !otherChannels.contains(channel) }
}

internal val PaymentChannel.amountConstraint: PaymentConstraint.Amount?
    get() = constraints
        .filterIsInstance<PaymentConstraint.Amount>()
        .firstOrNull()
