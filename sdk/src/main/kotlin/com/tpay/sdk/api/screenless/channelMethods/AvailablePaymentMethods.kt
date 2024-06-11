package com.tpay.sdk.api.screenless.channelMethods

import android.os.Parcelable
import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.InstallmentPayment
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.extensions.containsInstallment
import com.tpay.sdk.extensions.containsWallet
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing payment methods that are available
 * and safe to use, meaning that payment constraints were checked.
 *
 * @param [creditCardMethod] not null if credit card is available
 * @param [blikMethod] not null if BLIK is available
 * @param [availableTransfers] list containing available transfer methods
 * @param [availableWallets] list containing available digital wallet methods
 * @param [availablePekaoInstallmentMethods] list containing available Pekao installment methods
 * @param [payPoMethod] not null if PayPo is available
 */
@Parcelize
data class AvailablePaymentMethods(
    val creditCardMethod: ChannelMethod?,
    val blikMethod: ChannelMethod?,
    val availableTransfers: List<ChannelMethod>,
    val availableWallets: List<WalletMethod>,
    val availablePekaoInstallmentMethods: List<ChannelMethod>,
    val payPoMethod: ChannelMethod?
): Parcelable {
    companion object {
        /**
         * Method responsible for creating [AvailablePaymentMethods].
         * Resulting object will contain a common part of methods from [grouped] and [methods].
         *
         * @param [grouped] grouped payment channels created with [GroupedPaymentChannels.from] method
         * @param [methods] payment methods that you want to use
         * @param [amount] final price that will be used while creating the transaction,
         * payment channels will be filtered to satisfy the [PaymentConstraint.Amount] constraint
         */
        fun from(
            grouped: GroupedPaymentChannels,
            methods: List<PaymentMethod>,
            amount: Double
        ): AvailablePaymentMethods {
            val isGooglePayInMethods = methods.containsWallet(DigitalWallet.GOOGLE_PAY)
            val isPekaoInstallmentInMethods =
                methods.containsInstallment(InstallmentPayment.RATY_PEKAO)
            val isPayPoInMethods = methods.containsInstallment(InstallmentPayment.PAY_PO)

            val creditCardMethod = if (methods.contains(PaymentMethod.Card)) {
                grouped.getCreditCardIfAvailable(amount)?.asChannelMethod
            } else null

            val blikMethod = if (methods.contains(PaymentMethod.Blik)) {
                grouped.getBLIKIfAvailable(amount)?.asChannelMethod
            } else null

            val payPoMethod = if (isPayPoInMethods) {
                grouped.getAvailableInstallmentMethods(InstallmentPayment.PAY_PO, amount)
                    .firstOrNull()?.asChannelMethod
            } else null

            val availableTransfers = if (methods.contains(PaymentMethod.Pbl)) {
                grouped.getAvailableTransfers(amount).map { channel -> channel.asChannelMethod }
            } else emptyList()

            val availableWallets = mutableListOf<WalletMethod>().apply {
                if (isGooglePayInMethods) {
                    grouped.getGooglePayIfAvailable(amount)?.let { channel ->
                        add(WalletMethod(DigitalWallet.GOOGLE_PAY, channel.asChannelMethod))
                    }
                }
            }

            val availablePekaoInstallments = if (isPekaoInstallmentInMethods) {
                grouped.getAvailableInstallmentMethods(InstallmentPayment.RATY_PEKAO, amount)
                    .map { channel -> channel.asChannelMethod }
            } else emptyList()

            return AvailablePaymentMethods(
                creditCardMethod = creditCardMethod,
                blikMethod = blikMethod,
                availableTransfers = availableTransfers,
                availableWallets = availableWallets,
                availablePekaoInstallmentMethods = availablePekaoInstallments,
                payPoMethod = payPoMethod
            )
        }
    }
}

/**
 * Class responsible for storing basic information about wallet method
 */
@Parcelize
data class WalletMethod(
    val wallet: DigitalWallet,
    val method: ChannelMethod
) : Parcelable

/**
 * Class responsible for storing basic information about channel
 */
@Parcelize
data class ChannelMethod(
    val channelId: Int,
    val name: String,
    val imageUrl: String
) : Parcelable
