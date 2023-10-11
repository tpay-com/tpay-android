package com.tpay.sdk.api.models.transaction

import com.tpay.sdk.api.models.PayerContext
import com.tpay.sdk.api.screenless.Notifications

/**
 * Interface defining transactions
 */
interface Transaction {
    /**
     * payment amount/price, multiple of 0.01
     */
    val amount: Double

    /**
     * description visible for payer
     */
    val description: String

    /**
     * Information about payer
     */
    val payerContext: PayerContext

    /**
     * Information about notifications
     */
    val notifications: Notifications?
}
