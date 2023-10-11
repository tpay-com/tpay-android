@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonRoundIconBinding
import com.tpay.sdk.extensions.px


internal class ButtonRoundIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ButtonRoundIconBinding.inflate(LayoutInflater.from(context), this, true)

    var icon: Drawable?
        get() = binding.icon.drawable
        set(value) {
            value?.let {
                binding.icon.setImageDrawable(it)
            }
        }

    var buttonType: RoundIconButtonType = RoundIconButtonType.COLORFUL
        set(value) {
            binding.icon.run {
                background = ContextCompat.getDrawable(context, value.background)
                setPadding(
                    value.iconPadding,
                    value.iconPadding,
                    value.iconPadding,
                    value.iconPadding
                )
            }
            field = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ButtonRoundIcon).apply {
                try {
                    icon = getDrawable(R.styleable.ButtonRoundIcon_tpayButtonIcon)

                    getInteger(
                        R.styleable.ButtonRoundIcon_roundIconButtonType,
                        RoundIconButtonType.COLORFUL.ordinal
                    ).let {
                        buttonType =
                            if (it == RoundIconButtonType.GRAY.ordinal) RoundIconButtonType.GRAY
                            else RoundIconButtonType.COLORFUL
                    }
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.icon.run {
            isEnabled = enabled
            if (buttonType == RoundIconButtonType.COLORFUL) {
                imageTintList = ColorStateList.valueOf(
                    (ContextCompat.getColor(
                        context,
                        if (enabled) R.color.colorPrimary500 else R.color.colorNeutral300
                    ))
                )
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.icon.setOnClickListener(l)
    }

    enum class RoundIconButtonType(val background: Int, val iconPadding: Int) {
        COLORFUL(R.drawable.button_round_icon_states, 8.px),
        GRAY(R.drawable.button_round_icon_gray_states, 4.px)
    }
}