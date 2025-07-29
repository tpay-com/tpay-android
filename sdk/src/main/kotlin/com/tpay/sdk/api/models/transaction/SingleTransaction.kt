package com.tpay.sdk.api.models.transaction

import com.tpay.sdk.api.models.PayerContext
import com.tpay.sdk.api.screenless.Notifications
import kotlinx.parcelize.Parcelize

@Parcelize
data class SingleTransaction(
    override val amount: Double,
    override val description: String,
    override val hiddenDescription: String? = null,
    override val payerContext: PayerContext,
    override val notifications: Notifications?
) : Transaction
