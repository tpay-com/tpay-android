package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonPayCardBinding
import com.tpay.sdk.extensions.isVisible


internal class ButtonPayCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr){
    private val binding = ButtonPayCardBinding.inflate(LayoutInflater.from(context), this, true)

    var payCardBrand: PayCardBrand = PayCardBrand.NONE
        set(value) {
            binding.run {
                brandIcon = ContextCompat.getDrawable(context, value.iconId)
                cardBrand = context.getString(value.brandNameId)
            }

            field = value
        }

    var brandIcon: Drawable?
        get() = binding.icon.drawable
        set(value) {
            binding.icon.run {
                isVisible = value != null
                setImageDrawable(value)
            }
        }

    var cardBrand: String?
        get() = binding.brandName.text.toString()
        set(value) {
            binding.brandName.run {
                isVisible = value != null
                text = value
            }
        }

    var dottedCardNumber: String?
        get() = binding.cardNumber.text.toString()
        set(value) {
            binding.cardNumber.run {
                isVisible = value != null
                text = value
            }
        }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    override fun isSelected(): Boolean {
        return binding.root.isSelected
    }

    override fun setSelected(selected: Boolean) {
        binding.run {
            root.isSelected = selected
            radioButton.isChecked = selected
        }
    }

    enum class PayCardBrand(val iconId: Int, val brandNameId: Int) {
        MASTERCARD(R.drawable.ic_mastercard_24, R.string.mastercard),
        VISA(R.drawable.ic_visa_24, R.string.visa),
        NONE(R.drawable.ic_unknown_pay_card, R.string.pay_card)
    }
}