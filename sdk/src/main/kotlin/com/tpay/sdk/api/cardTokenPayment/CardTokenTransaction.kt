package com.tpay.sdk.api.cardTokenPayment

import android.os.Parcelable
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.screenless.Notifications
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing data about credit card token transaction.
 * @param [amount] amount of money customer will have to pay
 * @param [description] transaction description visible to customer
 * @param [hiddenDescription] hidden description visible for merchant
 * @param [payer] information about customer
 * @param [cardToken] credit card token
 * @param [notifications] notification information including email and url
 */
@Parcelize
data class CardTokenTransaction(
    val amount: Double,
    val description: String,
    val hiddenDescription: String? = null,
    val payer: Payer,
    val cardToken: String,
    val notifications: Notifications?
) : Parcelable
