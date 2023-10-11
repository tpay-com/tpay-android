@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonPaymentMethodBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.px
import com.tpay.sdk.extensions.updateMargins


internal class ButtonPaymentMethod @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding =
        ButtonPaymentMethodBinding.inflate(LayoutInflater.from(context), this, true)

    var name: String
        get() = binding.name.text.toString()
        set(value) {
            binding.name.text = value
        }

    var icon: Drawable?
        get() = binding.icon.drawable
        set(value) {
            binding.run {
                icon.setImageDrawable(value)
                icon.isVisible = value != null
                name.updateMargins(start = NAME_MARGIN_START)
            }
        }

    var paymentMethodPreset: PaymentMethodPreset? = null
        set(value) {
            value?.let {
                icon = ContextCompat.getDrawable(context, value.icon)
                name = context.getString(value.methodNameId)
                field = value
            }
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ButtonPaymentMethod).apply {
                try {
                    getInteger(R.styleable.ButtonPaymentMethod_preset, -1).let {
                        when (it) {
                            PaymentMethodPreset.APPLE_PAY.ordinal -> paymentMethodPreset =
                                PaymentMethodPreset.APPLE_PAY
                            PaymentMethodPreset.GOOGLE_PAY.ordinal -> paymentMethodPreset =
                                PaymentMethodPreset.GOOGLE_PAY
                            PaymentMethodPreset.PAYPAL.ordinal -> paymentMethodPreset =
                                PaymentMethodPreset.PAYPAL
                            else -> {
                                icon = getDrawable(R.styleable.ButtonPaymentMethod_tpayButtonIcon)
                                name = getString(R.styleable.ButtonPaymentMethod_buttonText) ?: ""
                            }
                        }
                    }
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    override fun setSelected(selected: Boolean) {
        binding.run {
            root.isSelected = selected
            radioButton.isChecked = selected
        }
    }

    enum class PaymentMethodPreset(val icon: Int, val methodNameId: Int) {
        APPLE_PAY(R.drawable.ic_apple_pay_24, R.string.payment_method_name_apple_pay),
        GOOGLE_PAY(R.drawable.ic_gpay_24, R.string.payment_method_name_google_pay),
        PAYPAL(R.drawable.ic_paypal_24, R.string.payment_method_name_pay_pal)
    }

    companion object {
        private val NAME_MARGIN_START = 14.px
    }
}