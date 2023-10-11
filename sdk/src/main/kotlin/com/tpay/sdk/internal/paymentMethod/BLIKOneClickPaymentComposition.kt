package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.isVisible


internal class BLIKOneClickPaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.BLIK_ONE_CLICK

        isLayoutVisible = true
    }

    override fun onDestroy() {
        isLayoutVisible = false
    }

    private var isLayoutVisible: Boolean = false
        set(value) {
            binding.run {
                paymentBoxBLIK.isSelected = value
                blikPaymentMethod.root.isVisible = value
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = true
            }
            field = value
        }
}