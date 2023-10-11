package com.tpay.sdk.internal.addCard

import android.content.Context
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentAddCardBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.extensions.runOnMainThread
import com.tpay.sdk.internal.paymentMethod.Composition


internal class NFCScanComposition(
    private val binding: FragmentAddCardBinding,
    private val viewModel: AddCardViewModel,
    private val context: Context
) : Composition(context) {
    override fun onCreate() {
        isLayoutVisible = true
        isScanning = true
        viewModel.shouldReadPayCardData.value = true

        binding.run {
            isBottomLayoutVisible = false
            nfcScan.run {
                cardNFCScanBackButton.text = context.getString(R.string.go_back)
                cardToPhoneTextView.text =
                    context.getString(R.string.place_credit_card_close_to_phone)
                dataReadingTextView.text =
                    context.getString(R.string.data_will_be_read_automatically)
                tryAgainButton.text = context.getString(R.string.try_again)
            }
        }

        observeViewModelFields()
        setOnClicks()
    }

    override fun onDestroy() {
        viewModel.shouldReadPayCardData.value = false
        isLayoutVisible = false
    }

    private fun setOnClicks() {
        binding.nfcScan.tryAgainButton.onClick {
            isScanning = true
            viewModel.shouldReadPayCardData.value = true
        }
    }

    private fun observeViewModelFields() {
        binding.nfcScan.run {
            viewModel.run {
                wasNFCScanSuccessful.observe { wasSuccessful ->
                    if (wasSuccessful) {
                        cardNFCScanBackButton.performClick()
                    } else {
                        isScanning = false
                        shouldReadPayCardData.value = false
                    }
                }
            }
        }
    }

    private var isLayoutVisible: Boolean
        get() = binding.nfcScan.root.isVisible
        set(value) {
            binding.nfcScan.root.isVisible = value
        }

    private var isScanning: Boolean
        get() = binding.nfcScan.progressBar.isVisible
        set(value) {
            runOnMainThread {
                binding.nfcScan.run {
                    progressBar.isVisible = value
                    tryAgainButton.isVisible = !value
                    dataReadingTextView.run {
                        text =
                            getStringOrNull(if (value) R.string.data_will_be_read_automatically else R.string.can_not_read_pay_card_data)
                        setTextColor(getColor(if (value) R.color.colorNeutral500 else R.color.colorSemanticError))
                    }
                }
            }
        }
}