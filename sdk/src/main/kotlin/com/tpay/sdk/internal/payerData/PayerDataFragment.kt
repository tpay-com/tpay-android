package com.tpay.sdk.internal.payerData

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPayerDataBinding
import com.tpay.sdk.designSystem.textfields.TextFieldAbstract
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.LegalNotesToSpan
import com.tpay.sdk.internal.base.BaseFragment
import java.util.*


internal class PayerDataFragment : BaseFragment(R.layout.fragment_payer_data) {
    override val binding by viewBinding(FragmentPayerDataBinding::bind)
    override val viewModel = PayerDataViewModel()

    private var modifiedContext: Context? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedLocale = languageSwitcher.localeObservable.value ?: Locale.getDefault()
        modifiedContext = if(selectedLocale == Locale.getDefault()) {
            null
        } else {
            getModifiedContextForLocale(selectedLocale)
        }

        sheetFragment.run {
            isSheetHeaderVisible = true
            hideUserCard()
            showLanguageBtn()
            isSheetHeaderVisible = true
        }

        applyLanguage(languageSwitcher.localeObservable.value ?: Locale.getDefault())

        binding.run {
            nameSurnameTextField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            emailTextField.setImeOptions(EditorInfo.IME_ACTION_DONE)
        }

        observeTextChanges()
        observeViewModelFields()
        selectPaymentMethodButtonClick()
        observeLanguageChanges()
        setupTextFieldValidation()
    }

    private fun observeLanguageChanges(){
        languageSwitcher.localeObservable.observe(this::applyLanguage)
    }

    private fun applyLanguage(locale: Locale){
        val ctx = getModifiedContextForLocale(locale)
        modifiedContext = ctx
        binding.run {
            sheetFragment.setHeaderText(ctx.getString(R.string.your_data), withAnim = false)
            rodoTextView.run {
                text = prepareBoldSpannedURLText(
                    LegalNotesToSpan.prepareRODOTextsToSpan(ctx, languageSwitcher.currentLanguage),
                    requireContext()
                )
                movementMethod = LinkMovementMethod.getInstance()
            }
            selectPaymentMethodButton.text = ctx.getString(R.string.payer_data_primary_button_text)
            nameSurnameTextField.run {
                modifiedContext = ctx
                hint = ctx.getString(R.string.payer_data_name_surname_hint)
                errorMessage = null
            }
            emailTextField.run {
                hint = ctx.getString(R.string.payer_data_email_hint)
                modifiedContext = ctx
                errorMessage = null
            }
        }
    }

    private fun selectPaymentMethodButtonClick() {
        binding.run {
            selectPaymentMethodButton.onClick {
                nameSurnameTextField.clearFocus()
                emailTextField.clearFocus()
                viewModel.onSelectPaymentMethodButtonClick()
            }
        }
    }

    private fun setupTextFieldValidation() {
        binding.nameSurnameTextField.setInputValidator(object : TextFieldAbstract.InputValidator {
            override fun validate(value: String): String? {
                val stringId = when {
                    !value.isFirstAndLastNameLengthValid -> R.string.invalid_number_of_characters
                    !value.isValidFirstAndLastName() -> R.string.first_last_name_invalid
                    else -> null
                }

                return stringId?.let { (modifiedContext ?: requireContext()).getString(stringId) }
            }
        })
    }

    private fun observeTextChanges() {
        binding.run {
            nameSurnameTextField.text.observe { text ->
                viewModel.nameSurname = text.formatted
            }
            emailTextField.text.observe { text ->
                viewModel.email = text.formatted
            }
        }
    }

    private fun observeViewModelFields() {
        viewModel.run {
            binding.run {
                nameSurnameTextField.formattedText = nameSurname
                emailTextField.formattedText = email

                emailError.observe { formError ->
                    emailTextField.errorMessage = getError(formError)
                }
                nameSurnameError.observe { formError ->
                    nameSurnameTextField.errorMessage = getError(formError)
                }

                buttonLoading.observe { isLoading ->
                    selectPaymentMethodButton.isLoading = isLoading
                }
            }
        }
    }

    private fun getError(formError: FormError): String? {
        return if (formError is FormError.Resource) {
            getModifiedContextForLocale(
                languageSwitcher.localeObservable.value ?: Locale.getDefault()
            ).getString(formError.id)
        } else null
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onFragmentDestroy()
        languageSwitcher.localeObservable.dispose()
    }
}