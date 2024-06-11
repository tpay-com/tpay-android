package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.designSystem.cards.ChannelMethodCard
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.internal.SheetFragment


internal class TransferPaymentComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val sheetFragment: SheetFragment,
    private val payButtonText: String,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.TRANSFER

        isLayoutVisible = true
        isTransferErrorVisible = false
        setupRecyclerView()
        observeErrors()
    }

    override fun onDestroy() {
        viewModel.run {
            selectedTransferId = null
            transferError.run {
                dispose()
                value = false
            }
        }
        isLayoutVisible = false
        try {
            sheetFragment.isShadowBelowHeaderVisible = false
        } catch (exception: Exception){ }
    }

    private var isLayoutVisible: Boolean
        get() = binding.transferPaymentMethod.root.isVisible
        set(value) {
            binding.run {
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = false
                paymentBoxTransfer.isSelected = value
                transferPaymentMethod.root.isVisible = value
                setPayButtonText(payButtonText)
            }
        }

    private fun setupRecyclerView() {
        val bankAdapter = ChannelMethodAdapter(ChannelMethodCard.Type.TRANSFER)
        binding.transferPaymentMethod.bankListRecyclerView.run {
            adapter = bankAdapter
            layoutManager =
                GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }

        bankAdapter.onMethodWithImageItemClickListener = ChannelMethodAdapter.OnMethodWithImageItemClickListener(viewModel::onTransferItemClick)
        bankAdapter.items = viewModel.availableTransferMethods
    }

    private fun observeErrors() {
        viewModel.transferError.observe { isError ->
            isTransferErrorVisible = isError
        }
    }

    private var isTransferErrorVisible: Boolean
        get() = binding.transferPaymentMethod.errorIcon.isVisible
        set(value) {
            binding.transferPaymentMethod.run {
                errorIcon.isVisible = value
                selectYourBankTextView.setTextColor(
                    if (value) getColor(R.color.colorSemanticError)
                    else getColor(R.color.colorPrimary900)
                )
            }
        }
}