package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import com.tpay.sdk.R
import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick

internal class WalletPaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val payButtonText: String,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.WALLET

        isLayoutVisible = true
        setWalletPaymentMethodActions()
        observeErrors()
        observeWalletMethods()
        selectWalletIfOnlyOneAvailable()
    }

    override fun onDestroy() {
        viewModel.run {
            walletMethod.run {
                dispose()
                value = WalletMethod.NONE
            }
            deselectWalletMethods()
            walletError.run {
                dispose()
                value = false
            }
        }
        isLayoutVisible = false
    }

    private var isLayoutVisible: Boolean
        get() = binding.walletPaymentMethod.root.isVisible
        set(value) {
            isWalletErrorVisible = !value
            binding.run {
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = false
                walletPaymentMethod.root.isVisible = value
                paymentBoxWallet.isSelected = value
                setPayButtonText(payButtonText)
            }
            if(value) deselectWalletMethods()
        }

    private fun selectWalletIfOnlyOneAvailable() = viewModel.run {
        if (availableWalletMethods.size != 1) return@run

        binding.walletPaymentMethod.run {
            when (availableWalletMethods.first().wallet) {
                DigitalWallet.GOOGLE_PAY -> {
                    googlePay.isSelected = true
                    walletMethod.value = WalletMethod.GOOGLE_PAY
                }
                // TODO: Add more wallets in the future
            }
        }
    }

    private fun observeWalletMethods(){
        viewModel.walletMethod.observe { method ->
            binding.walletPaymentMethod.run {
                googlePay.isSelected = method == WalletMethod.GOOGLE_PAY
            }
        }
    }

    private fun observeErrors() {
        viewModel.walletError.observe { isError ->
            isWalletErrorVisible = isError
        }
    }

    private fun setWalletPaymentMethodActions() {
        binding.walletPaymentMethod.run {
            googlePay.onClick {
                viewModel.walletMethod.value = WalletMethod.GOOGLE_PAY
            }
        }
    }

    private var isWalletErrorVisible: Boolean
        get() = binding.walletPaymentMethod.walletErrorIcon.isVisible
        set(value) {
            binding.walletPaymentMethod.run {
                walletErrorIcon.isVisible = value
                walletPayWithTextView.setTextColor(
                    if (value) getColor(R.color.colorSemanticError)
                    else getColor(R.color.colorPrimary900)
                )
            }
        }

    private fun deselectWalletMethods() {
        binding.walletPaymentMethod.run {
            googlePay.isSelected = false
            payPal.isSelected = false
        }
    }
}