package com.tpay.sdk.internal.failureStatus

import com.tpay.sdk.internal.SheetFragment
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseViewModel


internal class FailureStatusViewModel : BaseViewModel() {
    fun onCancelButtonClicked(sheet: SheetFragment) {
        when (sheet.sheetType) {
            SheetType.TOKENIZATION -> {
                addCardCoordinator.addCardFailure.invoke()
                repository.tokenizationId = null
            }
            else -> {
                paymentCoordinators.get(sheet.sheetType)?.paymentCancelled?.invoke(repository.transactionId)
                repository.transactionId = null
                repository.selectedPaymentMethod = null
            }
        }
        sheet.closeSheet()
    }

    fun onRetryButtonClicked(sheet: SheetFragment) {
        if (sheet.sheetType == SheetType.TOKEN_PAYMENT) {
            paymentCoordinators.get(sheet.sheetType)?.paymentCancelled?.invoke(repository.transactionId)
            sheet.closeSheet()
        } else {
            navigation.onBackPressed()
        }
        repository.transactionId = null
        repository.selectedPaymentMethod = null
    }
}