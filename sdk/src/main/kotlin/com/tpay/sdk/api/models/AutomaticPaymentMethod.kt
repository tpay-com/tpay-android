package com.tpay.sdk.api.models

import android.os.Parcelable
import com.tpay.sdk.api.paycard.CreditCardBrand
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing automatic payment methods.
 * @param [blikAlias] BLIK alias that will be used to register/pay (if registered)
 * @param [tokenizedCards] tokenized cards created after successful credit card payment
 */
@Parcelize
class AutomaticPaymentMethods(
    val blikAlias: BlikAlias? = null,
    val tokenizedCards: List<TokenizedCard> = emptyList()
) : Parcelable

/**
 * Class responsible for storing tokenized card data
 * @param [token] credit card token
 * @param [cardTail] 4 last numbers of credit card number
 * @param [brand] credit card brand
 */
@Parcelize
data class TokenizedCard(
    val token: String,
    val cardTail: String,
    val brand: CreditCardBrand
) : Parcelable

/**
 * Class responsible for storing BLIK alias data
 */
sealed class BlikAlias(open val value: String, open val label: String?) : Parcelable {
    /**
     * Indicates that BLIK alias is registered and ready to be used as a payment method.
     */
    @Parcelize
    class Registered(override val value: String, override val label: String? = null) : BlikAlias(value, label)

    /**
     * Indicates that BLIK alias needs to be registered with standard 6-digit BLIK payment
     */
    @Parcelize
    data class NotRegistered(override val value: String, override val label: String? = null) : BlikAlias(value, label)
}