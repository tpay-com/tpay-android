package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding

internal class PayPoComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val payButtonText: String,
    private val onHideKeyboard: () -> Unit,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.PAY_PO
        viewModel.createPayPoPayer()

        resetTextFields()

        isLayoutVisible = true

        setupTextFields()
        observeTextFieldErrors()
        observeTextChanges()
    }

    override fun onDestroy() {
        isLayoutVisible = false
        viewModel.run {
            deletePayPoPayer()
            payPoPayerNameError.dispose()
            payPoPayerEmailError.dispose()
            payPoPayerAddressError.dispose()
            payPoPayerPostalCodeError.dispose()
            payPoPayerCityError.dispose()
        }
        resetTextFields()
        onHideKeyboard()
    }

    private fun setupTextFields() = binding.payPoPayment.run {
        countryTextField.isEnabled = false

        nameSurnameTextField.run {
            setImeOptions(EditorInfo.IME_ACTION_NEXT)
            formattedText = viewModel.payPoPayerName
        }
        emailTextField.run {
            setImeOptions(EditorInfo.IME_ACTION_NEXT)
            formattedText = viewModel.payPoPayerEmail
        }
        addressTextField.run {
            setImeOptions(EditorInfo.IME_ACTION_NEXT)
            formattedText = viewModel.payPoPayerAddress
        }
        postalCodeTextField.run {
            setImeOptions(EditorInfo.IME_ACTION_NEXT)
            formattedText = viewModel.payPoPayerPostalCode
        }
        cityTextField.run {
            setImeOptions(EditorInfo.IME_ACTION_DONE)
            formattedText = viewModel.payPoPayerCity
        }
    }

    private fun resetTextFields() = binding.payPoPayment.run {
        nameSurnameTextField.reset()
        emailTextField.reset()
        addressTextField.reset()
        postalCodeTextField.reset()
        cityTextField.reset()
    }

    private fun observeTextChanges() = binding.payPoPayment.run {
        nameSurnameTextField.text.observe { text ->
            viewModel.payPoPayerName = text.notFormatted
        }
        emailTextField.text.observe { text ->
            viewModel.payPoPayerEmail = text.notFormatted
        }
        addressTextField.text.observe { text ->
            viewModel.payPoPayerAddress = text.notFormatted
        }
        postalCodeTextField.text.observe { text ->
            viewModel.payPoPayerPostalCode = text.notFormatted
        }
        cityTextField.text.observe { text ->
            viewModel.payPoPayerCity = text.notFormatted
        }
    }

    private fun observeTextFieldErrors() = binding.payPoPayment.run {
        viewModel.run {
            payPoPayerNameError.observe { formError ->
                nameSurnameTextField.errorMessage = getError(formError)
            }
            payPoPayerEmailError.observe { formError ->
                emailTextField.errorMessage = getError(formError)
            }
            payPoPayerAddressError.observe { formError ->
                addressTextField.errorMessage = getError(formError)
            }
            payPoPayerPostalCodeError.observe { formError ->
                postalCodeTextField.errorMessage = getError(formError)
            }
            payPoPayerCityError.observe { formError ->
                cityTextField.errorMessage = getError(formError)
            }
        }
    }

    private var isLayoutVisible: Boolean
        get() = binding.payPoPayment.root.isVisible
        set(value) {
            binding.run {
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = false
                paymentBoxPayPo.isSelected = value
                payPoPayment.root.isVisible = value
                setPayButtonText(payButtonText)
            }
        }
}