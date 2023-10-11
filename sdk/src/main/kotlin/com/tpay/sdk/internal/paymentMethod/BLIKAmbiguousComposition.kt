package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.isVisible


internal class BLIKAmbiguousComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.BLIK_AMBIGUOUS
        isLayoutVisible = true
        isBLIKAmbiguousErrorVisible = false

        observeErrors()
        setupRecyclerView()
    }

    override fun onDestroy() {
        viewModel.run {
            currentAmbiguousAlias = null
            ambiguousBlikAliases = emptyList()
            ambiguousBlikTransactionId = null
        }
        isLayoutVisible = false
    }

    private fun setupRecyclerView(){
        binding.blikBankPayment.blikRecycler.run {
            adapter = AmbiguousBLIKAdapter().apply {
                items = viewModel.ambiguousBlikAliases
                ambiguousBlikOnClickListener = AmbiguousBLIKAdapter.AmbiguousBLIKOnClickListener { ambiguousAlias ->
                    viewModel.currentAmbiguousAlias = ambiguousAlias
                }
            }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeErrors(){
        viewModel.oneClickBLIKError.observe { isError ->
            isBLIKAmbiguousErrorVisible = isError
        }
    }

    private var isBLIKAmbiguousErrorVisible: Boolean
        get() = binding.blikBankPayment.errorIcon.isVisible
        set(value) {
            binding.blikBankPayment.run {
                errorIcon.isVisible = value
                blikPayWith.setTextColor(
                    if (value) getColor(R.color.colorSemanticError)
                    else getColor(R.color.colorPrimary900)
                )
            }
        }

    private var isLayoutVisible: Boolean
        get() = binding.blikBankPayment.root.isVisible
        set(value) {
            binding.run {
                isBottomLayoutVisible = value
                paymentBoxBLIK.isSelected = value
                blikBankPayment.root.isVisible = value
                isPayWithCodeButtonVisible = true
            }
        }
}