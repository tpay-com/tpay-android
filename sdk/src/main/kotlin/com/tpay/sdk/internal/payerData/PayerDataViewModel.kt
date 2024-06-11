package com.tpay.sdk.internal.payerData

import com.tpay.sdk.R
import com.tpay.sdk.api.screenless.channelMethods.AvailablePaymentMethods
import com.tpay.sdk.api.screenless.channelMethods.GetPaymentChannels
import com.tpay.sdk.api.screenless.channelMethods.GetPaymentChannelsResult
import com.tpay.sdk.api.screenless.channelMethods.GroupedPaymentChannels
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.paymentMethod.PaymentMethodFragment


internal class PayerDataViewModel : BaseViewModel() {
    internal val nameSurnameError = Observable<FormError>(FormError.None)
    internal val emailError = Observable<FormError>(FormError.None)
    var nameSurname = ""
    var email = ""

    init {
        repository.run {
            configuration.merchant?.authorization?.run {
                setAuth(this, configuration.environment)
            }
            transaction.payerContext.payer.run {
                nameSurname = name
                this@PayerDataViewModel.email = email
            }
        }
    }

    fun onSelectPaymentMethodButtonClick() {
        isRequestInProgress = true
        nameSurnameError.value = when {
            nameSurname.isBlank() -> FormError.Resource(R.string.field_required)
            !nameSurname.isValidFirstAndLastName() -> FormError.Resource(R.string.first_last_name_invalid)
            !nameSurname.isFirstAndLastNameLengthValid -> FormError.Resource(R.string.invalid_number_of_characters)
            else -> FormError.None
        }
        emailError.value = when {
            email.isBlank() -> FormError.Resource(R.string.field_required)
            !email.isValidEmailAddress() -> FormError.Resource(R.string.email_address_not_valid)
            else -> FormError.None
        }

        if (emailError.value == FormError.None && nameSurnameError.value == FormError.None) {
            repository.transaction.payerContext.payer.run {
                name = nameSurname
                email = this@PayerDataViewModel.email
            }

            GetPaymentChannels().execute(this::handlePaymentChannelsResult)
        } else {
            isRequestInProgress = false
        }
    }

    private fun handlePaymentChannelsResult(result: GetPaymentChannelsResult) {
        when (result) {
            is GetPaymentChannelsResult.Success -> {
                val grouped = GroupedPaymentChannels.from(result.channels)
                val availablePaymentMethods = AvailablePaymentMethods.from(
                    grouped,
                    configuration.paymentMethods,
                    repository.transaction.amount
                )
                repository.availablePaymentMethods = availablePaymentMethods

                navigation.changeFragment(PaymentMethodFragment(), addToBackStack = true)
                screenClickable.value = true
            }
            is GetPaymentChannelsResult.Error -> {
                showSomethingWentWrong()
            }
        }
    }

    fun onFragmentDestroy(){
        disposeBag.disposeAll()
    }
}