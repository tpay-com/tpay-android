package com.tpay.sdk.internal.successStatus

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentSuccessStatusBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.extensions.viewBinding
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseFragment


internal class SuccessStatusFragment : BaseFragment(R.layout.fragment_success_status) {
    override val binding: FragmentSuccessStatusBinding by viewBinding(FragmentSuccessStatusBinding::bind)
    override val viewModel: SuccessStatusViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetFragment.run {
            isSheetHeaderVisible = false
            hideUserCard(withAnim = false)
            hideLanguageBtn(withAnim = false)
        }

        setOnClicks()
        setupViews()
    }

    private fun setOnClicks() {
        binding.primaryButton.onClick {
            viewModel.onPrimaryButtonClick(sheetFragment.sheetType)
            sheetFragment.closeSheet()
        }
    }

    private fun setupViews() {
        binding.run {
            description.isVisible = sheetFragment.sheetType == SheetType.TOKENIZATION
            when (sheetFragment.sheetType) {
                SheetType.PAYMENT, SheetType.TOKEN_PAYMENT -> {
                    headline.text = getString(R.string.payment_successful)
                    primaryButton.text = getString(R.string.my_orders)
                }
                SheetType.TOKENIZATION -> {
                    headline.text = getString(R.string.card_has_been_added)
                    primaryButton.text = getString(R.string.ok)
                    description.text = getString(R.string.you_can_now_use_fast_payment)
                }
                SheetType.WEB_VIEW -> { /* view not displayed */ }
            }
        }
    }
}