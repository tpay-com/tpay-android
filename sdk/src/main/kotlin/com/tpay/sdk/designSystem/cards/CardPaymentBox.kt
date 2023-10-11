@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.cards

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.CardPaymentBoxBinding


@SuppressLint("ClickableViewAccessibility")
internal class CardPaymentBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : LinearLayout(context, attrs, defAttrStyle) {
    private val binding = CardPaymentBoxBinding.inflate(LayoutInflater.from(context), this, true)

    var name: String
        get() = binding.name.text.toString()
        set(value) {
            binding.name.text = value
        }

    var iconDrawable: Drawable?
        get() = binding.icon.drawable
        set(value) {
            binding.icon.setImageDrawable(value)
        }

    var type: PaymentBoxType = PaymentBoxType.CARD
        set(value) {
            binding.icon.run {
                when (state) {
                    PaymentBoxState.ENABLED, PaymentBoxState.PRESSED -> {
                        setImageDrawable(ContextCompat.getDrawable(context, value.defaultIcon))
                    }
                    PaymentBoxState.CHECKED, PaymentBoxState.CHECKED_PRESSED -> {
                        setImageDrawable(ContextCompat.getDrawable(context, value.activeIcon))
                    }
                }
            }
            field = value
        }

    var state: PaymentBoxState = PaymentBoxState.ENABLED
        set(value) {
            binding.run {
                root.background = ContextCompat.getDrawable(context, value.background)
                name.setTextColor(ContextCompat.getColor(context, value.textColor))
            }
            field = value
            type = type
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.CardPaymentBox).apply {
                try {
                    name = getString(R.styleable.CardPaymentBox_buttonText) ?: ""
                    iconDrawable = getDrawable(R.styleable.CardPaymentBox_tpayButtonIcon)

                    type = when (getInteger(
                        R.styleable.CardPaymentBox_paymentBoxType,
                        PaymentBoxType.CARD.ordinal
                    )) {
                        PaymentBoxType.CARD.ordinal -> PaymentBoxType.CARD
                        PaymentBoxType.BLIK.ordinal -> PaymentBoxType.BLIK
                        PaymentBoxType.TRANSFER.ordinal -> PaymentBoxType.TRANSFER
                        else -> PaymentBoxType.WALLET
                    }

                    state = when (getInteger(
                        R.styleable.CardPaymentBox_paymentBoxState,
                        PaymentBoxState.ENABLED.ordinal
                    )) {
                        PaymentBoxState.CHECKED.ordinal -> PaymentBoxState.CHECKED
                        else -> PaymentBoxState.ENABLED
                    }
                } finally {
                    recycle()
                }
            }
        }

        binding.root.setOnTouchListener { _, motionEvent ->
            state = when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    when (state) {
                        PaymentBoxState.ENABLED -> PaymentBoxState.PRESSED
                        else -> PaymentBoxState.CHECKED_PRESSED
                    }
                }
                MotionEvent.ACTION_UP -> {
                    when (state) {
                        PaymentBoxState.PRESSED, PaymentBoxState.CHECKED_PRESSED -> PaymentBoxState.CHECKED
                        else -> PaymentBoxState.ENABLED
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    when (state) {
                        PaymentBoxState.PRESSED -> PaymentBoxState.ENABLED
                        else -> PaymentBoxState.CHECKED
                    }
                }
                else -> state
            }
            false
        }
    }

    override fun setSelected(selected: Boolean) {
        binding.root.isSelected = selected
        state = if (selected) PaymentBoxState.CHECKED else PaymentBoxState.ENABLED
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    enum class PaymentBoxType(val defaultIcon: Int, val activeIcon: Int) {
        CARD(R.drawable.ic_mastercard_visa, R.drawable.ic_mastercard_visa),
        BLIK(R.drawable.ic_blik_default, R.drawable.ic_blik_active),
        TRANSFER(R.drawable.ic_transfer_default, R.drawable.ic_transfer_active),
        WALLET(R.drawable.ic_wallet_default, R.drawable.ic_wallet_active)
    }

    enum class PaymentBoxState(val background: Int, val textColor: Int) {
        ENABLED(R.drawable.payment_box_enabled, R.color.colorPrimary900),
        PRESSED(R.drawable.payment_box_pressed, R.color.colorPrimary900),
        CHECKED(R.drawable.payment_box_checked, R.color.colorNeutralWhite),
        CHECKED_PRESSED(R.drawable.payment_box_checked_pressed, R.color.colorNeutralWhite)
    }
}