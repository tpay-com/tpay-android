package com.tpay.sdk.extensions

import com.tpay.sdk.server.dto.parts.ConstraintDTO

internal fun List<ConstraintDTO>.firstOrNullOfType(type: String): ConstraintDTO? {
    return firstOrNull { dto -> dto.type == type }
}