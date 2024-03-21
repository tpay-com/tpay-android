package com.tpay.sdk.api.screenless.channelMethods

/**
 * Class storing information about payment channel
 * @param [id] id of the payment channel
 * @param [name] channel display name
 * @param [imageUrl] channel image url
 * @param [isAvailable] whether the channel is currently available to use
 * @param [isOnline] whether the channel supports a online payment
 * @param [isInstantRedirectionAvailable] whether the channel supports transaction creation with instant redirection
 * @param [groups] payment groups available to use with legacy transaction creation system
 * @param [constraints] channel constraints
 */
data class PaymentChannel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isAvailable: Boolean,
    val isOnline: Boolean,
    val isInstantRedirectionAvailable: Boolean,
    val groups: List<PaymentGroup>,
    val constraints: List<PaymentConstraint>
) {
    val asChannelMethod: ChannelMethod
        get() = ChannelMethod(
            channelId = id.toInt(),
            name = name,
            imageUrl = imageUrl
        )
}

/**
 * Class storing information about a payment group
 * @param [id] id of the group
 * @param [name] group display name
 * @param [imageUrl] group image url
 */
data class PaymentGroup(
    val id: String,
    val name: String,
    val imageUrl: String
)

/**
 * Class storing information about a payment constraint.
 * Filter payment methods according to the constraints, to avoid errors while creating payments.
 */
sealed class PaymentConstraint {
    /**
     * Indicates that there is a payment amount constraint.
     * Multiple configurations are supported, meaning that this constraint can
     * have only [minimum], [maximum] or both values.
     * @param [minimum] if not null, minimum payment amount supported
     * @param [maximum] if not null, maximum payment amount supported
     */
    data class Amount(val minimum: Double?, val maximum: Double?) : PaymentConstraint() {
        /**
         * Function responsible for checking if the provided [amount]
         * is between [minimum] and [maximum]
         */
        fun check(amount: Double): Boolean {
            val x = minimum?.let { amount >= minimum } ?: true
            val y = maximum?.let { amount <= maximum } ?: true

            return x && y
        }
    }
}
