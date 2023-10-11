package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import android.view.inputmethod.EditorInfo
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.*
import com.tpay.sdk.extensions.isVisible


internal class BLIKCodePaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    context: Context,
    private val showBackButton: Boolean = false,
    private val onBackButtonClick: () -> Unit = { },
    private val onHideKeyboard: () -> Unit = { }
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.BLIK

        setupViews()
        isBackButtonVisible = showBackButton
        isLayoutVisible = true
        setupTextFields()
        setOnClicks()
    }

    override fun onDestroy() {
        isLayoutVisible = false
        isBackButtonVisible = false
        viewModel.run {
            blikNumberError.dispose()
            isSaveBlikChecked = false
            saveBlikLabel = ""
        }
        binding.blikCodePayment.blikTextField.reset()
        onHideKeyboard.invoke()
    }

    private fun setupViews() {
        binding.blikCodePayment.run {
            saveBlikLayout.isVisible = viewModel.canBlikAliasBeRegistered
            saveBlikCheckbox.isChecked = false
            saveBlikTextField.isVisible = false
        }
    }

    private fun setOnClicks(){
        binding.blikCodePayment.run {
            blikBackButton.onClick(onBackButtonClick)
            saveBlikCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isSaveBlikChecked = isChecked
                saveBlikTextField.isVisible = isChecked
            }
        }
    }

    private fun setupTextFields(){
        binding.blikCodePayment.run {
            blikTextField.run {
                reset()
                setImeOptions(EditorInfo.IME_ACTION_DONE)
                text.observe { text ->
                    viewModel.blikOtc = text.notFormatted
                }
                viewModel.blikNumberError.observe { formError ->
                    errorMessage = this@BLIKCodePaymentComposition.getError(formError)
                }
            }
            saveBlikTextField.run {
                reset()
                setImeOptions(EditorInfo.IME_ACTION_DONE)
                text.observe { text ->
                    viewModel.saveBlikLabel = text.notFormatted
                }
                isRequired = false
            }
        }
    }

    private var isBackButtonVisible: Boolean
        get() = binding.blikCodePayment.blikBackButton.isVisible
        set(value) {
            binding.blikCodePayment.run {
                blikBackButton.isVisible = value
                blikTextField.updateMargins(top = if(value) TEXT_FIELD_MARGIN_TOP else 0.px )
            }
        }

    private var isLayoutVisible: Boolean
        get() = binding.blikCodePayment.root.isVisible
        set(value) {
            binding.run {
                isBottomLayoutVisible = value
                paymentBoxBLIK.isSelected = value
                blikCodePayment.root.isVisible = value
                isPayWithCodeButtonVisible = false
            }
        }

    companion object {
        private val TEXT_FIELD_MARGIN_TOP = 6.px
    }
}