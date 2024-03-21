package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.isNFCAvailable
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.extensions.runOnMainThread


internal class CardPaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val onCardSelectionButtonClick: () -> Unit,
    private val onHideKeyboard: () -> Unit,
    private val context: Context,
    private val payButtonText: String
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.CARD

        resetTextFields()
        resetCheckbox()

        isLayoutVisible = true
        isNfcIconVisible = context.isNFCAvailable()
        isCardSelectionButtonVisible = viewModel.automaticCreditCardPaymentMethods.isNotEmpty()

        setupTextFields()
        observeTextChanges()
        observeTextFieldErrors()
        setOnClickListeners()
        observeNfcChanges()

        binding.methodPicker.run {
            post { scrollTo(0, 0) }
        }
    }

    override fun onDestroy() {
        viewModel.run {
            cardNumberError.dispose()
            cardCVVError.dispose()
            cardDateError.dispose()
            isNfcEnabled.dispose()
            isPayCardScanSuccessful.dispose()
            shouldReadPayCardData.value = false
        }
        resetCheckbox()
        resetTextFields()
        onHideKeyboard.invoke()
        isLayoutVisible = false
    }

    private var isLayoutVisible: Boolean
        get() = binding.cardPaymentMethod.root.isVisible
        set(value) {
            binding.run {
                isPayWithCodeButtonVisible = !value
                isBottomLayoutVisible = value
                paymentBoxCard.isSelected = value
                cardPaymentMethod.root.isVisible = value
                setPayButtonText(payButtonText)
                if (!value) isNFCLayoutVisible = false
            }
        }

    private var isCardSelectionButtonVisible: Boolean
        get() = binding.cardPaymentMethod.cardSelectionButton.isVisible
        set(value) {
            binding.cardPaymentMethod.run {
                cardSelectionButton.isVisible = value
                (creditCardNumberTextField.layoutParams as ConstraintLayout.LayoutParams).topToBottom =
                    if (value) cardSelectionButton.id else addCardText.id
                addCardText.isVisible = !value
            }
        }

    private var isScanning: Boolean
        get() = binding.cardNFCScan.progressBar.isVisible
        set(value) {
            runOnMainThread {
                binding.cardNFCScan.run {
                    progressBar.isVisible = value
                    tryAgainButton.isVisible = !value
                    dataReadingTextView.run {
                        setTextColor(getColor(if (value) R.color.colorNeutral500 else R.color.colorSemanticError))
                        text =
                            getStringOrNull(if (value) R.string.data_will_be_read_automatically else R.string.can_not_read_pay_card_data)
                    }
                }
            }
        }

    private var isNfcIconVisible: Boolean
        get() = binding.cardPaymentMethod.creditCardNumberTextField.isNfcIconVisible
        set(value) {
            binding.cardPaymentMethod.creditCardNumberTextField.isNfcIconVisible = value
        }

    private fun setOnClickListeners() {
        binding.run {
            cardPaymentMethod.run {
                cardSelectionButton.onClick {
                    onHideKeyboard.invoke()
                    onCardSelectionButtonClick.invoke()
                }
                saveCardCheckbox.onClick(viewModel::onCheckboxClick)

                creditCardNumberTextField.run {
                    onNfcIconClick {
                        onHideKeyboard.invoke()
                        viewModel.isNfcEnabled.value?.let { isEnabled ->
                            root.isVisible = false
                            isBottomLayoutVisible = false
                            if (isEnabled) {
                                isNFCLayoutVisible = true
                                isScanning = true
                                viewModel.shouldReadPayCardData.value = true
                            } else {
                                isEnableNFCLayoutVisible = true
                                isNFCLayoutVisible = false
                            }
                        }
                    }
                }
            }

            cardNFCScan.run {
                cardNFCScanBackButton.onClick {
                    isNFCLayoutVisible = false
                    isBottomLayoutVisible = true
                    isLayoutVisible = true
                    viewModel.shouldReadPayCardData.value = false
                }
                tryAgainButton.onClick {
                    isScanning = true
                    viewModel.shouldReadPayCardData.value = true
                }
            }

            enableNfc.run {
                enableNfcBackButton.onClick {
                    isEnableNFCLayoutVisible = false
                    isLayoutVisible = true
                }

                enableNfcSettingsButton.onClick {
                    context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
        }
    }

    private fun resetCheckbox() {
        viewModel.saveCardChecked = false
        binding.cardPaymentMethod.saveCardCheckbox.isChecked = false
    }

    private fun resetTextFields() {
        binding.cardPaymentMethod.run {
            creditCardNumberTextField.reset()
            creditCardDateTextField.reset()
            creditCardCVVTextField.reset()
        }
    }

    private fun setupTextFields() {
        binding.cardPaymentMethod.run {
            creditCardNumberTextField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            creditCardDateTextField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
            creditCardCVVTextField.setImeOptions(EditorInfo.IME_ACTION_DONE)
        }
    }

    private fun observeNfcChanges() {
        viewModel.run {
            isPayCardScanSuccessful.observe { isSuccessful ->
                if (isSuccessful) {
                    binding.cardNFCScan.cardNFCScanBackButton.performClick()
                } else {
                    shouldReadPayCardData.value = false
                    isScanning = false
                }
            }

            isNfcEnabled.observe { isEnabled ->
                if (isNFCLayoutVisible || isEnableNFCLayoutVisible) {
                    binding.run {
                        isBottomLayoutVisible = false
                        cardPaymentMethod.root.isVisible = false

                        isNFCLayoutVisible = isEnabled
                        isEnableNFCLayoutVisible = !isEnabled
                        if (isEnabled) isScanning = true
                    }
                }
            }
        }
    }

    private fun observeTextChanges() {
        binding.cardPaymentMethod.run {
            creditCardNumberTextField.text.observe { text ->
                text.run { viewModel.cardNumber = notFormatted }
            }
            creditCardDateTextField.text.observe { text ->
                text.run { viewModel.cardDate = formatted }
            }
            creditCardCVVTextField.text.observe { text ->
                text.run { viewModel.cardCVV = formatted }
            }
        }
    }

    private fun observeTextFieldErrors() {
        binding.cardPaymentMethod.run {
            viewModel.cardNumberError.observe { formError ->
                creditCardNumberTextField.errorMessage = this@CardPaymentComposition.getError(formError)
            }
            viewModel.cardDateError.observe { formError ->
                creditCardDateTextField.errorMessage = this@CardPaymentComposition.getError(formError)
            }
            viewModel.cardCVVError.observe { formError ->
                creditCardCVVTextField.errorMessage = this@CardPaymentComposition.getError(formError)
            }
        }
    }

    private var isNFCLayoutVisible: Boolean
        get() = binding.cardNFCScan.root.isVisible
        set(value) {
            binding.cardNFCScan.root.isVisible = value
        }

    private var isEnableNFCLayoutVisible: Boolean
        get() = binding.enableNfc.root.isVisible
        set(value) {
            binding.enableNfc.root.isVisible = value
        }
}