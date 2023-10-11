package com.tpay.sdk.internal.addCard

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentAddCardBinding
import com.tpay.sdk.extensions.isNFCAvailable
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.extensions.prepareBoldSpannedURLText
import com.tpay.sdk.internal.Language
import com.tpay.sdk.internal.LegalNotesToSpan
import com.tpay.sdk.internal.paymentMethod.Composition


internal class CardInformationComposition(
    val binding: FragmentAddCardBinding,
    val viewModel: AddCardViewModel,
    private val context: Context,
    private val language: Language
) : Composition(context) {
    override fun onCreate() {
        isLayoutVisible = true
        binding.run {
            isBottomLayoutVisible = true

            addCard.creditCardNumberTextField.isNfcIconVisible = context.isNFCAvailable()
        }

        observeTextFieldChanges()
        observeViewModelFields()
        setOnClicks()
        setupTextFields()
        setSpannedTexts()
    }

    override fun onDestroy() {
        isLayoutVisible = false
        viewModel.run {
            nameSurnameError.dispose()
            emailError.dispose()
            creditCardNumberError.dispose()
            expirationDateError.dispose()
            cvvError.dispose()
        }
        binding.addCard.run {
            addCardNameSurnameTextField.text.dispose()
            addCardEmailTextField.text.dispose()
            creditCardNumberTextField.text.dispose()
            creditCardDateTextField.text.dispose()
            creditCardCVVTextField.text.dispose()
        }
    }

    private var isLayoutVisible: Boolean
        get() = binding.addCard.root.isVisible
        set(value) {
            binding.addCard.root.isVisible = value
        }

    private fun setOnClicks() {
        binding.addCardButton.onClick {
            viewModel.onSaveCardButtonClick()
        }
    }

    private fun setSpannedTexts() {
        binding.rodoTextView.run {
            text = prepareBoldSpannedURLText(
                LegalNotesToSpan.prepareRODOTextsToSpan(
                    this@CardInformationComposition.context,
                    language
                ),
                this@CardInformationComposition.context
            )
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setupTextFields() {
        binding.addCard.run {
            addCardNameSurnameTextField.run {
                setImeOptions(EditorInfo.IME_ACTION_NEXT)
                modifiedContext = this@CardInformationComposition.context
                notFormattedText = viewModel.nameSurname
                hint =
                    this@CardInformationComposition.context.getString(R.string.payer_data_name_surname_hint)
            }
            addCardEmailTextField.run {
                setImeOptions(EditorInfo.IME_ACTION_NEXT)
                modifiedContext = this@CardInformationComposition.context
                notFormattedText = viewModel.email
                hint =
                    this@CardInformationComposition.context.getString(R.string.payer_data_email_hint)
            }
            creditCardNumberTextField.run {
                setImeOptions(EditorInfo.IME_ACTION_NEXT)
                modifiedContext = this@CardInformationComposition.context
                hint =
                    this@CardInformationComposition.context.getString(R.string.credit_card_number_hint)
            }
            creditCardDateTextField.run {
                setImeOptions(EditorInfo.IME_ACTION_NEXT)
                modifiedContext = this@CardInformationComposition.context
                hint =
                    this@CardInformationComposition.context.getString(R.string.credit_card_valid_date_hint)
            }
            creditCardCVVTextField.run {
                setImeOptions(EditorInfo.IME_ACTION_DONE)
                modifiedContext = this@CardInformationComposition.context
                hint =
                    this@CardInformationComposition.context.getString(R.string.credit_card_cvv_hint)
            }
        }
    }

    private fun observeTextFieldChanges() {
        binding.addCard.run {
            viewModel.run {
                addCardNameSurnameTextField.text.observe { text ->
                    nameSurname = text.formatted
                }
                addCardEmailTextField.text.observe { text ->
                    email = text.formatted
                }
                creditCardNumberTextField.text.observe { text ->
                    creditCardNumber = text.notFormatted
                }
                creditCardDateTextField.text.observe { text ->
                    expirationDate = text.formatted
                }
                creditCardCVVTextField.text.observe { text ->
                    cvv = text.formatted
                }
            }
        }
    }

    private fun observeViewModelFields() {
        binding.addCard.run {
            viewModel.run {
                nameSurnameError.observe { formError ->
                    addCardNameSurnameTextField.errorMessage = this@CardInformationComposition.getError(formError)
                }
                emailError.observe { formError ->
                    addCardEmailTextField.errorMessage = this@CardInformationComposition.getError(formError)
                }
                creditCardNumberError.observe { formError ->
                    creditCardNumberTextField.errorMessage = this@CardInformationComposition.getError(formError)
                }
                expirationDateError.observe { formError ->
                    creditCardDateTextField.errorMessage = this@CardInformationComposition.getError(formError)
                }
                cvvError.observe { formError ->
                    creditCardCVVTextField.errorMessage = this@CardInformationComposition.getError(formError)
                }
            }
        }
    }
}