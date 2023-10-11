package com.tpay.sdk.internal

import javax.inject.Singleton


@Singleton
internal class PaymentCoordinators {
    private val coordinators = hashMapOf<SheetType, PaymentCoordinator>()

    fun add(sheetType: SheetType, coordinator: PaymentCoordinator) {
        coordinators[sheetType] = coordinator
    }

    fun get(sheetType: SheetType): PaymentCoordinator? = coordinators[sheetType]
    fun remove(sheetType: SheetType) = coordinators.remove(sheetType)
}