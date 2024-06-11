package com.tpay.sdk.internal.cardTokenPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentProcessingPaymentBinding
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.internal.base.BaseFragment


internal class TokenPaymentProcessingFragment : BaseFragment(R.layout.fragment_processing_payment) {
    override val binding: FragmentProcessingPaymentBinding by viewBinding(FragmentProcessingPaymentBinding::bind)
    override val viewModel: TokenPaymentProcessingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetFragment.run {
            isSheetHeaderVisible = false
            hideUserCard(withAnim = false)
            hideLanguageBtn(withAnim = false)
        }

        viewModel.startTransaction()
    }
}