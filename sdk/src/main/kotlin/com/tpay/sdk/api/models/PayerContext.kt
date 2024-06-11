package com.tpay.sdk.api.models

import android.os.Parcelable
import com.tpay.sdk.api.models.payer.Payer
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing information about payer and his
 * automatic payment methods
 */
@Parcelize
data class PayerContext(
    val payer: Payer,
    val automaticPaymentMethods: AutomaticPaymentMethods? = null
) : Parcelable
