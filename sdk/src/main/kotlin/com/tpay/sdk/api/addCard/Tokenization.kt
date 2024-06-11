package com.tpay.sdk.api.addCard

import android.os.Parcelable
import com.tpay.sdk.api.models.payer.Payer
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing data about tokenization
 * @param [payer] data about payer
 * @param [notificationUrl] url that tokenization notifications will be sent to
 */
@Parcelize
data class Tokenization(
    val payer: Payer,
    val notificationUrl: String
) : Parcelable
