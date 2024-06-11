package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.designSystem.cards.ChannelMethodCard
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.internal.SheetFragment

internal class RatyPekaoComposition(
    private val binding: FragmentPaymentMethodBinding,
    private val viewModel: PaymentMethodViewModel,
    private val sheetFragment: SheetFragment,
    private val payButtonText: String,
    context: Context
) : Composition(context) {
    override fun onCreate() {
        viewModel.screenState = PaymentMethodScreenState.RATY_PEKAO

        isLayoutVisible = true
        isRatyPekaoErrorVisible = false

        setupRecyclerView()
        observeErrors()
    }

    override fun onDestroy() {
        viewModel.run {
            selectedRatyPekaoVariantId = null
            ratyPekaoError.run {
                dispose()
                value = false
            }
        }
        isLayoutVisible = false
        try {
            sheetFragment.isShadowBelowHeaderVisible = false
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        val pekaoAdapter = ChannelMethodAdapter(ChannelMethodCard.Type.RATY_PEKAO)
        binding.pekaoInstallmentsMethod.pekaoVariantRecyclerView.run {
            adapter = pekaoAdapter
            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }

        pekaoAdapter.onMethodWithImageItemClickListener =
            ChannelMethodAdapter.OnMethodWithImageItemClickListener(viewModel::onRatyPekaoItemClick)
        pekaoAdapter.items = viewModel.availableRatyPekaoMethods
    }

    private fun observeErrors() {
        viewModel.ratyPekaoError.observe { isError ->
            isRatyPekaoErrorVisible = isError
        }
    }

    private var isLayoutVisible: Boolean
        get() = binding.pekaoInstallmentsMethod.root.isVisible
        set(value) {
            binding.run {
                isBottomLayoutVisible = value
                isPayWithCodeButtonVisible = false
                paymentBoxRatyPekao.isSelected = value
                pekaoInstallmentsMethod.root.isVisible = value
                setPayButtonText(payButtonText)
            }
        }

    private var isRatyPekaoErrorVisible: Boolean
        get() = binding.pekaoInstallmentsMethod.errorIcon.isVisible
        set(value) {
            binding.pekaoInstallmentsMethod.run {
                errorIcon.isVisible = value
                selectPekaoVariantTextView.setTextColor(
                    if (value) getColor(R.color.colorSemanticError)
                    else getColor(R.color.colorPrimary900)
                )
            }
        }
}