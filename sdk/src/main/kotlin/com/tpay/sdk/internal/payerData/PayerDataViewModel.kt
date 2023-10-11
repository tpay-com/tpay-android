package com.tpay.sdk.internal.payerData

import android.graphics.drawable.BitmapDrawable
import com.tpay.sdk.R
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.TransactionMethodsUtil
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.model.TransactionMethods
import com.tpay.sdk.internal.paymentMethod.TransferMethod
import com.tpay.sdk.internal.paymentMethod.PaymentMethodFragment
import com.tpay.sdk.server.dto.response.GetTransactionMethodsResponseDTO


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
            repository.run {
                transaction.payerContext.payer.run {
                    name = nameSurname
                    email = this@PayerDataViewModel.email
                }

                getAvailablePaymentMethods().observe(
                    this@PayerDataViewModel::handlePaymentMethodsSuccess,
                    this@PayerDataViewModel::handleError
                ).disposedBy(disposeBag)
            }
        } else {
            isRequestInProgress = false
        }
    }

    private fun handlePaymentMethodsSuccess(getTransactionMethodsResponseDTO: GetTransactionMethodsResponseDTO){
        repository.run {
            val apiTransactionMethods = TransactionMethods.fromDTO(getTransactionMethodsResponseDTO)
            val configurationPaymentMethods = configuration.paymentMethods

            availableTransactionMethods = TransactionMethodsUtil.getAvailableMethods(apiTransactionMethods, configurationPaymentMethods)

            if (availableTransactionMethods.transfers.isEmpty()){
                transferMethods = emptyList()
                navigation.changeFragment(PaymentMethodFragment(), addToBackStack = true)
                screenClickable.value = true
            } else {
                availableTransactionMethods.transfers
                    .map { method -> Triple(method.id, method.name, getImageDrawable(method.imageUrl)) }
                    .observe { triples ->
                        transferMethods = triples.map { (id, name, image) ->
                            TransferMethod(id = id, name = name, icon = image)
                        }.sortedBy { it.name }
                        navigation.changeFragment(PaymentMethodFragment(), addToBackStack = true)
                        screenClickable.value = true
                    }
            }
        }
    }

    private fun List<Triple<String, String, Completable<BitmapDrawable>>>.observe(block: (List<Triple<String, String, BitmapDrawable?>>) -> Unit){
        val result = mutableListOf<Triple<String, String, BitmapDrawable?>>()
        forEach { (id, name, imageCompletable) ->
            imageCompletable
                .observe({ bitmapDrawable ->
                    result.add(Triple(id, name, bitmapDrawable))
                    if(result.size == size){
                        block.invoke(result)
                    }
                }, {
                    result.add(Triple(id, name, null))
                    if(result.size == size){
                        block.invoke(result)
                    }
                })
        }
    }

    fun onFragmentDestroy(){
        disposeBag.disposeAll()
    }
}