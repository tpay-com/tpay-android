package com.tpay.sdk.api.screenless.channelMethods

import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.toModel
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.config.Configuration
import javax.inject.Inject

class GetPaymentChannels {

    @Inject
    private lateinit var repository: Repository

    @Inject
    private lateinit var configuration: Configuration

    init {
        injectFields()
        val authorization = configuration.merchant?.authorization ?: throw IllegalStateException(
            MERCHANT_AUTHORIZATION_MISSING
        )
        repository.setAuth(authorization, configuration.environment)
    }

    fun execute(onResult: (GetPaymentChannelsResult) -> Unit) {
        repository
            .getPaymentChannels()
            .observe({ response ->
                val paymentChannels = response.channels.map { dto -> dto.toModel }

                onResult(
                    GetPaymentChannelsResult.Success(
                        currency = response.currency,
                        language = response.language,
                        channels = paymentChannels
                    )
                )
            }, { e ->
                onResult(GetPaymentChannelsResult.Error(e.message))
            })
    }

    companion object {
        private const val MERCHANT_AUTHORIZATION_MISSING =
            "Please provide merchant authorization via TpayModule."
    }
}