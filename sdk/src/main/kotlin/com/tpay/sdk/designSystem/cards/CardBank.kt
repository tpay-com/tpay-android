@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.cards

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.CardBankBinding
import com.tpay.sdk.extensions.isVisible


internal class CardBank @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : FrameLayout(context, attrs, defAttrStyle) {
    private val binding = CardBankBinding.inflate(LayoutInflater.from(context), this, true)

    var bankName: String
        get() = binding.bankName.text.toString()
        set(value) {
            binding.bankName.run {
                text = value
                isVisible = bankLogo == null
            }
        }

    var bankLogo: Drawable?
        get() = binding.bankLogo.drawable
        set(value) {
            binding.run {
                bankLogo.setImageDrawable(value)
                bankName.isVisible = value == null
            }
        }

    var cardState: BankCardState = BankCardState.ENABLED
        set(value) {
            binding.root.background = ContextCompat.getDrawable(context, value.backgroundId)
            field = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.CardBank).apply {
                bankLogo = getDrawable(R.styleable.CardBank_bankLogo)
                bankName = getString(R.styleable.CardBank_bankName) ?: ""
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        binding.root.isSelected = selected
        cardState = if (selected) BankCardState.CHECKED else BankCardState.ENABLED
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    enum class BankCardState(val backgroundId: Int) {
        ENABLED(R.drawable.bank_card_enabled),
        CHECKED(R.drawable.bank_card_checked)
    }
}