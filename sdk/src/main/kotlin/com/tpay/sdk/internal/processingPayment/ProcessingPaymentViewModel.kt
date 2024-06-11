package com.tpay.sdk.internal.processingPayment

import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.internal.CompletableScheduler
import com.tpay.sdk.internal.PaymentStatus
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.server.dto.response.GetTransactionResponseDTO


internal class ProcessingPaymentViewModel : BaseViewModel() {
    val isBlikPayment: Boolean
        get() = repository.selectedPaymentMethod is PaymentMethod.Blik

    private var scheduler: CompletableScheduler<GetTransactionResponseDTO>? = null

    fun init() {
        configuration.merchant?.authorization?.run {
            repository.setAuth(this, configuration.environment)
        }
        repository.transactionId?.let { id ->
            scheduler = CompletableScheduler { repository.getTransaction(id) }.apply {
                schedule(
                    REQUEST_DELAY,
                    { response ->
                        handleResponse(this, response)
                    }, { }
                )
            }
        }
    }

    private fun handleResponse(scheduler: CompletableScheduler<*>, response: GetTransactionResponseDTO) {
        try {
            when {
                PaymentStatus.SUCCESS_STATUSES.contains(response.status) -> {
                    scheduler.stop()
                    moveToSuccessScreen()
                }
                PaymentStatus.ERROR_STATUS == response.status || (isBlikPayment && response.isBlikError) -> {
                    scheduler.stop()
                    moveToFailureScreen()
                }
            }
        } catch (_: Exception) {}
    }

    fun onDestroy(){
        scheduler?.stop()
        scheduler = null
    }

    companion object {
        private const val REQUEST_DELAY = 6000L
    }
}