package com.tpay.sdk.api.screenless.blik

import com.tpay.sdk.api.models.BlikAlias
import com.tpay.sdk.api.screenless.LongPollingConfig
import com.tpay.sdk.api.screenless.Payment
import com.tpay.sdk.api.screenless.TransactionResponseValidator
import com.tpay.sdk.server.dto.request.ContinueBlikAliasTransactionDTO
import com.tpay.sdk.server.dto.request.PayTransactionRequestDTO

/**
 * Class responsible for continuing BLIK OneClick payment after ambiguous result
 */
class BLIKAmbiguousAliasPayment private constructor(
    private val transactionId: String,
    private val request: PayTransactionRequestDTO
) : Payment<CreateBLIKTransactionResult>() {
    override fun execute(
        longPollingConfig: LongPollingConfig?,
        onResult: (CreateBLIKTransactionResult) -> Unit
    ) {
        continueTransaction(transactionId, request)
            .observe({ response ->
                val result = TransactionResponseValidator.validateBLIK(response)

                longPollingConfig?.run {
                    if (result is CreateBLIKTransactionResult.Created) {
                        longPolling.start(
                            transactionId = result.transactionId,
                            longPollingConfig = this,
                            isBlikPayment = true
                        )
                    }
                }

                onResult(result)
            }, { e ->
                onResult(CreateBLIKTransactionResult.Error(e.message))
            })
    }

    companion object {
        /**
         * Function responsible for creating [BLIKAmbiguousAliasPayment].
         * @param [transactionId] id of transaction from [CreateBLIKTransactionResult.AmbiguousBlikAlias]
         * @param [blikAlias] blik alias used to create transaction with [BLIKPayment]
         * @param [ambiguousAlias] ambiguous alias selected by user
         */
        fun from(
            transactionId: String,
            blikAlias: BlikAlias,
            ambiguousAlias: AmbiguousAlias
        ): BLIKAmbiguousAliasPayment {
            return BLIKAmbiguousAliasPayment(
                transactionId = transactionId,
                request = ContinueBlikAliasTransactionDTO(blikAlias, ambiguousAlias)
            )
        }
    }
}