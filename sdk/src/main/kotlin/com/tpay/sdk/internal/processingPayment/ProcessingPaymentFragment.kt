package com.tpay.sdk.internal.processingPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentProcessingPaymentBinding
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.internal.base.BaseFragment


internal class ProcessingPaymentFragment : BaseFragment(R.layout.fragment_processing_payment) {
    override val binding: FragmentProcessingPaymentBinding by viewBinding(FragmentProcessingPaymentBinding::bind)
    override val viewModel: ProcessingPaymentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetFragment.run {
            isSheetHeaderVisible = false
            hideUserCard(withAnim = false)
            hideLanguageBtn(withAnim = false)
        }

        setTexts()
        sheetFragment.handleRestore(savedInstanceState)
        viewModel.init()
    }

    private fun setTexts() {
        binding.headline.text =
            getString(if (viewModel.isBlikPayment) R.string.confirm_payment_in_bank_app else R.string.processing_payment)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
}