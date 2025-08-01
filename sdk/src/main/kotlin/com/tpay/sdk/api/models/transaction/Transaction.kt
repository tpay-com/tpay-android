package com.tpay.sdk.api.models.transaction

import android.os.Parcelable
import com.tpay.sdk.api.models.PayerContext
import com.tpay.sdk.api.screenless.Notifications

/**
 * Interface defining transactions
 */
interface Transaction : Parcelable {
    /**
     * payment amount/price, multiple of 0.01
     */
    val amount: Double

    /**
     * description visible for payer
     */
    val description: String

    /**
     * hidden description visible for merchant
     */
    val hiddenDescription: String?

    /**
     * Information about payer
     */
    val payerContext: PayerContext

    /**
     * Information about notifications
     */
    val notifications: Notifications?
}
