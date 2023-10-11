package com.tpay.sdk.internal.confirmPayment

import android.os.Bundle
import android.view.View
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentConfirmPaymentBinding
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.internal.base.BaseFragment


internal class ConfirmPaymentFragment : BaseFragment(R.layout.fragment_confirm_payment) {
    override val binding: FragmentConfirmPaymentBinding by viewBinding(FragmentConfirmPaymentBinding::bind)
    override val viewModel = ConfirmPaymentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetFragment.isSheetHeaderVisible = false
    }
}