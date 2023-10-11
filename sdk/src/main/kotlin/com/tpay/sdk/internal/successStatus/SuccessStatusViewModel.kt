package com.tpay.sdk.internal.successStatus

import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseViewModel


internal class SuccessStatusViewModel : BaseViewModel() {
    fun onPrimaryButtonClick(sheetType: SheetType) {
        when (sheetType) {
            SheetType.TOKENIZATION -> {
                addCardCoordinator.addCardSuccess.invoke(repository.tokenizationId)
                repository.tokenizationId = null
            }
            else -> {
                paymentCoordinators.get(sheetType)?.paymentCompleted?.invoke(repository.transactionId)
                repository.transactionId = null
                repository.selectedPaymentMethod = null
            }
        }
    }
}