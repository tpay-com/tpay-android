package com.tpay.sdk.api.screenless.channelMethods

import com.tpay.sdk.api.models.InstallmentPayment
import com.tpay.sdk.extensions.amountConstraint
import com.tpay.sdk.extensions.channelWithGroupId
import com.tpay.sdk.extensions.channelsWithGroupId
import com.tpay.sdk.extensions.channelsWithout
import com.tpay.sdk.extensions.ignoreChannels
import com.tpay.sdk.extensions.removeChannelsWithEmptyGroups

/**
 * Class responsible for storing grouped payment channels.
 * @param [creditCardChannel] if not null, stores information about credit card channel
 * @param [blikChannel] if not null, stores information about BLIK channel
 * @param [transferChannels] stores information about transfer channels
 * @param [walletChannels] stores information about digital wallet channels
 * @param [installmentPayments] stores information about different kinds of installment payments
 */
data class GroupedPaymentChannels(
    val creditCardChannel: PaymentChannel?,
    val blikChannel: PaymentChannel?,
    val transferChannels: List<PaymentChannel>,
    val walletChannels: List<PaymentChannel>,
    val installmentPayments: List<InstallmentPaymentKind>
) {
    /**
     * Returns credit card channel if available for specified [amount]
     */
    fun getCreditCardIfAvailable(amount: Double): PaymentChannel? = creditCardChannel?.run {
        val isAvailable = isAvailable && (amountConstraint?.check(amount) ?: true)
        if (isAvailable) this else null
    }

    /**
     * Returns BLIK channel if available for specified [amount]
     */
    fun getBLIKIfAvailable(amount: Double): PaymentChannel? = blikChannel?.run {
        val isAvailable = isAvailable && (amountConstraint?.check(amount) ?: true)
        if (isAvailable) this else null
    }

    /**
     * Returns Google Pay channel if available for specified [amount]
     */
    fun getGooglePayIfAvailable(amount: Double): PaymentChannel? = getGooglePayChannel()?.run {
        val isAvailable = isAvailable && (amountConstraint?.check(amount) ?: true)
        return if (isAvailable) this else null
    }

    /**
     * Returns available transfer payment channels for specified [amount].
     */
    fun getAvailableTransfers(amount: Double): List<PaymentChannel> {
        return transferChannels.filter { channel ->
            channel.isAvailable && (channel.amountConstraint?.check(amount) ?: true)
        }
    }

    /**
     * Returns available installment payments of type [installmentPayment] for specified [amount].
     */
    fun getAvailableInstallmentMethods(
        installmentPayment: InstallmentPayment,
        amount: Double
    ): List<PaymentChannel> {
        return installmentPayments
            .firstOrNull { kind -> kind.installmentPayment == installmentPayment }
            ?.channels
            ?.filter { channel ->
                channel.isAvailable && (channel.amountConstraint?.check(amount) ?: true)
            } ?: emptyList()
    }

    private fun getGooglePayChannel(): PaymentChannel? {
        return walletChannels.firstOrNull { channel ->
            channel.id == GOOGLE_PAY_CHANNEL_ID
        }
    }

    companion object {
        private const val CREDIT_CARD_GROUP_ID = "103"
        private const val BLIK_GROUP_ID = "150"
        private const val RATY_PEKAO_GROUP_ID = "169"
        private const val GOOGLE_PAY_GROUP_ID = "166"
        private const val GOOGLE_PAY_CHANNEL_ID = "68"
        private const val APPLE_PAY_CHANNEL_ID = "75"
        private const val PAY_PO_GROUP_ID = "172"

        /**
         * This method groups payment channels, so they are easier to display.
         * @param [channels] channels received from [GetPaymentChannels.execute] method
         * @param [ignoreUnsupported] if true, ignores unsupported payment channels, currently
         * ignores only Apple Pay because it is not available on Android
         */
        fun from(channels: List<PaymentChannel>, ignoreUnsupported: Boolean = true) =
            channels
                .removeChannelsWithEmptyGroups()
                .ignoreChannels(if (ignoreUnsupported) listOf(APPLE_PAY_CHANNEL_ID) else emptyList())
                .toMutableList().run {
                    val creditCardChannel = channelWithGroupId(CREDIT_CARD_GROUP_ID)
                    val blikChannel = channelWithGroupId(BLIK_GROUP_ID)
                    val walletChannels =
                        channelWithGroupId(GOOGLE_PAY_GROUP_ID)?.let(::listOf) ?: emptyList()
                    val installmentChannels =
                        mutableListOf<InstallmentPaymentKind>().apply installments@{
                            val ratyPekaoChannels = channelsWithGroupId(RATY_PEKAO_GROUP_ID)
                            if (ratyPekaoChannels.isNotEmpty()) {
                                add(
                                    InstallmentPaymentKind(
                                        InstallmentPayment.RATY_PEKAO,
                                        ratyPekaoChannels
                                    )
                                )
                            }
                            channelWithGroupId(PAY_PO_GROUP_ID)?.run {
                                add(InstallmentPaymentKind(InstallmentPayment.PAY_PO, listOf(this)))
                            }
                        }

                    val transferChannels = channelsWithout(
                        mutableListOf(creditCardChannel, blikChannel).apply {
                            addAll(walletChannels)
                            addAll(installmentChannels.map { kind -> kind.channels }.flatten())
                        }
                    )

                    GroupedPaymentChannels(
                        creditCardChannel = creditCardChannel,
                        blikChannel = blikChannel,
                        transferChannels = transferChannels,
                        walletChannels = walletChannels,
                        installmentPayments = installmentChannels
                    )
                }
    }
}

/**
 * Class responsible for storing information about
 * payment [channels] available for [installmentPayment] kind.
 */
data class InstallmentPaymentKind(
    val installmentPayment: InstallmentPayment,
    val channels: List<PaymentChannel>
)
