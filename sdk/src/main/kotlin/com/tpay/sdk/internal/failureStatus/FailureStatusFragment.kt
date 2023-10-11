package com.tpay.sdk.internal.failureStatus

import android.os.Bundle
import android.view.View
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentFailureStatusBinding
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseFragment


internal class FailureStatusFragment : BaseFragment(R.layout.fragment_failure_status) {
    override val binding: FragmentFailureStatusBinding by viewBinding(FragmentFailureStatusBinding::bind)
    override val viewModel = FailureStatusViewModel()

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
        binding.run {
            retryButton.onClick {
                viewModel.onRetryButtonClicked(sheetFragment)
            }
            cancelButton.onClick {
                viewModel.onCancelButtonClicked(sheetFragment)
            }
        }
    }

    private fun setupViews() {
        binding.run {
            val isTokenPayment = sheetFragment.sheetType == SheetType.TOKEN_PAYMENT
            cancelButton.isVisible = !isTokenPayment
            retryButton.updateMargins(bottom = if (isTokenPayment) TOKEN_PAYMENT_BUTTON_MARGIN_BOTTOM else 0.px)

            when (sheetFragment.sheetType) {
                SheetType.PAYMENT -> {
                    headline.text = getString(R.string.payment_failure)
                    retryButton.text = getString(R.string.retry_payment)
                }
                SheetType.TOKEN_PAYMENT -> {
                    headline.text = getString(R.string.payment_failure)
                    retryButton.text = getString(R.string.ok)
                }
                SheetType.TOKENIZATION -> {
                    headline.text = getString(R.string.card_has_not_been_added)
                    retryButton.text = getString(R.string.try_again)
                    description.text = getString(R.string.try_again_or_select_different_method)
                }
            }
        }
    }

    companion object {
        private val TOKEN_PAYMENT_BUTTON_MARGIN_BOTTOM = 30.px
    }
}