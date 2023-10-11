package com.tpay.sdk.api.screenless


/**
 * Class responsible for storing long polling config
 *
 * @param [delayMillis] delay between each request
 * @param [maxRequestCount] maximum number of requests to be sent, null = no limit
 * @param [stopOnFirstRequestError] stop long polling on first error
 * @param [onTransactionState] function called every time there is a successful response
 * from server with current [TransactionState] of transaction
 * @param [onRequestError] function called when unexpected error occurs (server or client side)
 * @param [onMaxRequestCount] function called when [maxRequestCount] is achieved
 */
data class LongPollingConfig(
    val delayMillis: Long,
    val maxRequestCount: Int? = null,
    val stopOnFirstRequestError: Boolean = false,
    val onTransactionState: (TransactionState) -> Unit,
    val onRequestError: (Exception) -> Unit,
    val onMaxRequestCount: () -> Unit = { }
)
