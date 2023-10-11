package com.tpay.sdk.api.screenless

import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.Completable
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO
import com.tpay.sdk.server.dto.request.PayTransactionRequestDTO
import com.tpay.sdk.server.dto.response.CreateTransactionResponseDTO
import javax.inject.Inject


abstract class Payment <T> {

    @Inject
    private lateinit var repository: Repository

    @Inject
    private lateinit var configuration: Configuration

    protected val longPolling = LongPolling()

    init {
        injectFields()
        val merchantAuthorization = configuration.merchant?.authorization ?: throw IllegalStateException(NO_AUTH_ERROR_MESSAGE)
        val environment = configuration.environment
        repository.setAuth(merchantAuthorization, environment)
    }

    /**
     * Function responsible for creating transaction and starting long polling mechanism.
     *
     * @param [longPollingConfig] configuration of long polling mechanism, null = no long polling
     * @param [onResult] function called with result of transaction creation
     */
    abstract fun execute(
        longPollingConfig: LongPollingConfig? = null,
        onResult: (T) -> Unit
    )

    internal fun makeTransaction(request: CreateTransactionRequestDTO): Completable<CreateTransactionResponseDTO> {
        return repository.createTransaction(request)
    }

    internal fun continueTransaction(
        transactionId: String,
        request: PayTransactionRequestDTO
    ): Completable<CreateTransactionResponseDTO> {
        return repository.continueTransaction(transactionId, request)
    }

    companion object {
        const val NO_AUTH_ERROR_MESSAGE = "Configure merchant authorization using TpayModule before using this payment."
    }
}