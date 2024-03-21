package com.tpay.sdk.internal

import com.tpay.sdk.api.screenless.channelMethods.PaymentConstraint
import com.tpay.sdk.extensions.firstOrNullOfType
import com.tpay.sdk.server.dto.parts.ConstraintDTO

internal object PaymentConstraintMapper {
    private const val AMOUNT_CONSTRAINT = "amount"
    private const val MIN_TYPE = "min"
    private const val MAX_TYPE = "max"

    fun getConstraints(dtos: List<ConstraintDTO>): List<PaymentConstraint> {
        if (dtos.isEmpty()) return emptyList()
        return mutableListOf<PaymentConstraint>().apply {
            getAmountConstraint(dtos)?.run(this::add)
        }
    }

    private fun getAmountConstraint(dtos: List<ConstraintDTO>): PaymentConstraint.Amount? {
        val constraints = dtos.filter { dto -> dto.field == AMOUNT_CONSTRAINT }

        if (constraints.isEmpty()) return null

        return try {
            PaymentConstraint.Amount(
                minimum = constraints.firstOrNullOfType(MIN_TYPE)?.value?.toDouble(),
                maximum = constraints.firstOrNullOfType(MAX_TYPE)?.value?.toDouble()
            )
        } catch (exception: Exception) {
            null
        }
    }
}