package com.tpay.sdk.internal.webView

import com.tpay.sdk.extensions.Observable
import com.tpay.sdk.internal.CompletableScheduler
import com.tpay.sdk.internal.PaymentStatus
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.server.dto.response.GetTransactionResponseDTO


internal class WebViewViewModel : BaseViewModel(){
    private var scheduler: CompletableScheduler<GetTransactionResponseDTO>? = null
    val isTransactionFinished = Observable(false)

    private var wasTransactionHandled: Boolean
        get() = isTransactionFinished.value ?: false
        set(value) {
            isTransactionFinished.value = value
        }

    fun getUrl(): String? = repository.webUrl?.url
    fun getSuccessUrl(): String = repository.internalRedirects.successUrl
    fun getErrorUrl(): String = repository.internalRedirects.errorUrl

    fun init() {
        configuration.merchant?.authorization?.run {
            repository.setAuth(this, configuration.environment)
        }
        repository.transactionId?.let { id ->
            scheduler = CompletableScheduler { repository.getTransaction(id) }.apply {
                schedule(
                    REQUEST_DELAY,
                    { response ->
                        when {
                            PaymentStatus.SUCCESS_STATUSES.contains(response.status) -> {
                                onSuccess()
                            }
                            PaymentStatus.ERROR_STATUS == response.status -> {
                                onError()
                            }
                        }
                    }, { }
                )
            }
        }
    }

    private fun handleTransaction(block: () -> Unit = { }){
        if(!wasTransactionHandled){
            wasTransactionHandled = true
            stopScheduler()
            block()
        }
    }

    fun onSuccess() = handleTransaction {
        moveToSuccessScreen()
    }

    fun onError() = handleTransaction {
        moveToFailureScreen()
    }

    private fun stopScheduler(){
        scheduler?.stop()
        scheduler = null
    }

    fun onDestroy(){
        handleTransaction()
    }

    companion object {
        private const val REQUEST_DELAY = 10000L
    }
}