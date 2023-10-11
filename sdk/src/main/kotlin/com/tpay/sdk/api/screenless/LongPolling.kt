package com.tpay.sdk.api.screenless

import com.tpay.sdk.di.injectFields
import com.tpay.sdk.internal.CompletableScheduler
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.server.dto.response.GetTransactionResponseDTO
import javax.inject.Inject


/**
 * Class responsible for monitoring transaction state. When started with [start] it continues to send requests
 * even when network exceptions occur.
 *
 * Stops automatically when [TransactionState] is CORRECT, PAID, ERROR or DECLINED.
 * Can be stopped with [stop] method.
 * Stops automatically when maximum number of requests from [LongPollingConfig] is achieved.
 */
class LongPolling {

    @Inject
    private lateinit var repository: Repository

    init {
        injectFields()
    }

    private var scheduler: CompletableScheduler<GetTransactionResponseDTO>? = null
    private var requestCounter = 0

    /**
     * Function responsible for starting long polling on transaction with specified [transactionId]
     */
    fun start(
        transactionId: String,
        longPollingConfig: LongPollingConfig,
        isBlikPayment: Boolean = false
    ) {
        stop()
        scheduler = CompletableScheduler { repository.getTransaction(transactionId) }
        longPollingConfig.run {
            scheduler?.schedule(delayMillis, { response ->
                try {
                    val currentState = TransactionState.values()
                        .first { state -> state.name.lowercase() == response.status?.lowercase() }
                        .let { state ->
                            if (isBlikPayment && response.isBlikError) TransactionState.ERROR
                            else state
                        }
                    if (currentState in listOf(
                            TransactionState.PAID,
                            TransactionState.CORRECT,
                            TransactionState.ERROR,
                            TransactionState.DECLINED
                        )
                    ) {
                        stop()
                    }
                    onTransactionState(currentState)
                } catch (exception: Exception) {
                    onTransactionState(TransactionState.UNKNOWN)
                }
                requestCounter++
                if (requestCounter == maxRequestCount) {
                    stop()
                    onMaxRequestCount()
                }
            }, { e ->
                if(stopOnFirstRequestError){
                    stop()
                }
                onRequestError(e)
                requestCounter++
                if (requestCounter == maxRequestCount) {
                    stop()
                    onMaxRequestCount()
                }
            })
        }
    }

    fun stop() {
        scheduler?.stop()
        scheduler = null
        requestCounter = 0
    }
}