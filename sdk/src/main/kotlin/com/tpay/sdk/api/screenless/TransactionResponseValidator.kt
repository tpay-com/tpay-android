package com.tpay.sdk.api.screenless

import com.tpay.sdk.api.screenless.blik.AmbiguousAlias
import com.tpay.sdk.api.screenless.blik.CreateBLIKTransactionResult
import com.tpay.sdk.api.screenless.card.CreateCreditCardTransactionResult
import com.tpay.sdk.api.screenless.googlePay.CreateGooglePayTransactionResult
import com.tpay.sdk.api.screenless.pekaoInstallment.CreatePekaoInstallmentTransactionResult
import com.tpay.sdk.api.screenless.transfer.CreateTransferTransactionResult
import com.tpay.sdk.server.dto.readMessages
import com.tpay.sdk.server.dto.response.CreateTransactionResponseDTO

/**
 * Class responsible for validation of transaction create response
 */
internal class TransactionResponseValidator {
    companion object {
        private const val TRANSACTION_ID_NULL = "Transaction id null in Tpay response"
        private const val PAYMENT_URL_NULL = "Payment url null in Tpay response"
        private const val TRANSACTION_SEEMS_TO_EXIST = "Transaction seems to exist but is not successful and paymentUrl is null"
        private const val BLIK_PAYMENT_INCORRECT_STATE = "BLIK transaction is in incorrect state"

        fun validateBLIK(response: CreateTransactionResponseDTO): CreateBLIKTransactionResult {
            val transactionId = response.transactionId
                ?: return CreateBLIKTransactionResult.Error(TRANSACTION_ID_NULL)

            val errors = response.payments?.errors
            val alternatives = response.payments?.alternatives

            return when {
                TransactionState.SUCCESS_STATES.contains(response.status) -> {
                    CreateBLIKTransactionResult.CreatedAndPaid(transactionId)
                }
                response.status == TransactionState.PENDING.actual -> {
                    return when {
                        !alternatives.isNullOrEmpty() -> {
                            val ambiguousAliases = alternatives.map { alternative ->
                                AmbiguousAlias(
                                    name = alternative.name,
                                    code = alternative.code
                                )
                            }

                            CreateBLIKTransactionResult.AmbiguousBlikAlias(transactionId, ambiguousAliases)
                        }
                        errors.isNullOrEmpty() -> {
                            CreateBLIKTransactionResult.Created(transactionId)
                        }
                        else -> {
                            CreateBLIKTransactionResult.ConfiguredPaymentFailed(
                                transactionId,
                                response.payments?.errors?.readMessages()
                            )
                        }
                    }
                }
                else -> {
                    CreateBLIKTransactionResult.Error(
                        errorMessage = BLIK_PAYMENT_INCORRECT_STATE,
                        transactionId = transactionId
                    )
                }
            }
        }

        fun validateGooglePay(response: CreateTransactionResponseDTO): CreateGooglePayTransactionResult {
            val transactionId = response.transactionId
                ?: return CreateGooglePayTransactionResult.Error(errorMessage = TRANSACTION_ID_NULL)

            val paymentUrl = response.transactionPaymentUrl

            return when {
                TransactionState.SUCCESS_STATES.contains(response.status) -> {
                    CreateGooglePayTransactionResult.CreatedAndPaid(transactionId)
                }
                paymentUrl != null -> {
                    CreateGooglePayTransactionResult.Created(transactionId, paymentUrl)
                }
                else -> {
                    CreateGooglePayTransactionResult.Error(
                        errorMessage = TRANSACTION_SEEMS_TO_EXIST,
                        transactionId = transactionId
                    )
                }
            }
        }

        fun validateCreditCard(response: CreateTransactionResponseDTO): CreateCreditCardTransactionResult {
            val transactionId = response.transactionId
                ?: return CreateCreditCardTransactionResult.Error(errorMessage = TRANSACTION_ID_NULL)

            val paymentUrl = response.transactionPaymentUrl

            return when {
                TransactionState.SUCCESS_STATES.contains(response.status) -> {
                    CreateCreditCardTransactionResult.CreatedAndPaid(transactionId)
                }
                paymentUrl != null -> {
                    CreateCreditCardTransactionResult.Created(transactionId, paymentUrl)
                }
                else -> {
                    CreateCreditCardTransactionResult.Error(
                        errorMessage = TRANSACTION_SEEMS_TO_EXIST,
                        transactionId = transactionId
                    )
                }
            }
        }

        fun validateTransfer(response: CreateTransactionResponseDTO): CreateTransferTransactionResult {
            val transactionId = response.transactionId
                ?: return CreateTransferTransactionResult.Error(TRANSACTION_ID_NULL)

            val paymentUrl = response.transactionPaymentUrl
                ?: return CreateTransferTransactionResult.Error(
                    errorMessage = PAYMENT_URL_NULL,
                    transactionId = transactionId
                )

            return CreateTransferTransactionResult.Created(transactionId, paymentUrl)
        }

        fun validatePekaoInstallment(response: CreateTransactionResponseDTO): CreatePekaoInstallmentTransactionResult {
            val transactionId = response.transactionId
                ?: return CreatePekaoInstallmentTransactionResult.Error(TRANSACTION_ID_NULL)

            val paymentUrl = response.transactionPaymentUrl
                ?: return CreatePekaoInstallmentTransactionResult.Error(
                    devErrorMessage = PAYMENT_URL_NULL,
                    transactionId = transactionId
                )

            return CreatePekaoInstallmentTransactionResult.Created(transactionId, paymentUrl)
        }
    }
}