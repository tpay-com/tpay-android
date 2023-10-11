package com.tpay.sdk.internal.cardTokenPayment

import com.tpay.sdk.api.screenless.PaymentDetails
import com.tpay.sdk.api.screenless.card.CreateCreditCardTransactionResult
import com.tpay.sdk.api.screenless.card.CreditCardPayment
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.webView.WebUrl


internal class TokenPaymentProcessingViewModel : BaseViewModel() {
    init {
        configuration.merchant?.authorization?.let { auth ->
            repository.setAuth(auth, configuration.environment)
        }
    }

    fun startTransaction() {
        screenClickable.value = false

        val cardTokenTransaction = repository.cardTokenTransaction ?: kotlin.run {
            moveToFailureScreen()
            screenClickable.value = true
            return
        }

        CreditCardPayment.Builder().apply {
            cardTokenTransaction.run {
                setCreditCardToken(cardToken)
                setPaymentDetails(PaymentDetails(amount, description))
                setPayer(payer)
                setCallbacks(repository.internalRedirects, notifications)
            }
        }.build().execute(onResult = this::handleTokenResult)
    }

    private fun handleTokenResult(result: CreateCreditCardTransactionResult) {
        when (result) {
            is CreateCreditCardTransactionResult.CreatedAndPaid -> {
                handleTransactionId(result.transactionId)
                moveToSuccessScreen()
            }
            is CreateCreditCardTransactionResult.Created -> {
                handleTransactionId(result.transactionId)
                repository.webUrl = WebUrl.Payment(result.paymentUrl)
                moveToWebViewScreen()
            }
            is CreateCreditCardTransactionResult.Error -> {
                result.transactionId?.let(this::handleTransactionId)
                moveToFailureScreen()
            }
        }

        screenClickable.value = true
    }

    private fun handleTransactionId(id: String) {
        paymentCoordinators.get(SheetType.TOKEN_PAYMENT)?.paymentCreated?.invoke(id)
        repository.transactionId = id
    }
}