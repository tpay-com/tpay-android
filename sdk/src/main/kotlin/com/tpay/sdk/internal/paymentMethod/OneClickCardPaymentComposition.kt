package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.internal.SheetFragment


internal class OneClickCardPaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val sheetFragment: SheetFragment,
    private val onAddCardClick: () -> Unit,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.CARD_ONE_CLICK

        setOnClickListeners()
        setupRecyclerView()
        observeViewModelFields()
        isLayoutVisible = true
        isErrorVisible = false

        binding.methodPicker.run {
            post { scrollTo(0, 0) }
        }
    }

    override fun onDestroy() {
        isLayoutVisible = false
        viewModel.selectedTokenizedCard = null
        try {
            sheetFragment.isShadowBelowHeaderVisible = false
        } catch (exception: Exception){ exception.printStackTrace() }
    }

    private fun setupRecyclerView(){
        val creditCardAdapter = CreditCardAdapter()
        binding.oneClickCardPayment.payCardRecyclerView.run {
            adapter = creditCardAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
        creditCardAdapter.creditCardItemListener = CreditCardAdapter.CreditCardItemListener { tokenizedCard ->
            viewModel.selectedTokenizedCard = tokenizedCard
        }
        creditCardAdapter.items = viewModel.automaticCreditCardPaymentMethods
    }

    private fun observeViewModelFields(){
        viewModel.oneClickCardError.observe { isError ->
            isErrorVisible = isError
        }
    }

    private fun setOnClickListeners(){
        binding.oneClickCardPayment.addCardButton.onClick(onAddCardClick)
    }

    private var isErrorVisible: Boolean
        get() = binding.oneClickCardPayment.errorIcon.isVisible
        set(value) {
            binding.oneClickCardPayment.run {
                errorIcon.isVisible = value
                selectCardText.setTextColor(
                    if (value) getColor(R.color.colorSemanticError)
                    else getColor(R.color.colorPrimary900)
                )
            }
        }

    private var isLayoutVisible: Boolean
        get() = binding.oneClickCardPayment.root.isVisible
        set(value) {
            binding.run {
                oneClickCardPayment.root.isVisible = value
                paymentBoxCard.isSelected = value
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = false
            }
        }
}