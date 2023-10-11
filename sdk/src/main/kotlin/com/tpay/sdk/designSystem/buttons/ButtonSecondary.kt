@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonSecondaryBinding
import com.tpay.sdk.extensions.isVisible


internal class ButtonSecondary @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding =
        ButtonSecondaryBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String
        get() = binding.buttonText.text.toString()
        set(value) {
            binding.buttonText.text = value
        }

    var icon: Drawable?
        get() = binding.iconRight.drawable
        set(value) {
            binding.run {
                iconRight.setImageDrawable(value)
                iconLeft.setImageDrawable(value)
            }
        }

    var iconPosition: IconPosition
        get() = if (binding.iconLeft.isVisible) IconPosition.START else IconPosition.END
        set(value) {
            binding.run {
                iconLeft.isVisible = value == IconPosition.START
                iconRight.isVisible = value == IconPosition.END
            }
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ButtonSecondary).apply {
                try {
                    text = getString(R.styleable.ButtonSecondary_buttonText) ?: ""
                    icon = getDrawable(R.styleable.ButtonSecondary_tpayButtonIcon)

                    getInteger(
                        R.styleable.ButtonSecondary_iconPosition,
                        IconPosition.END.ordinal
                    ).let {
                        iconPosition =
                            if (it == IconPosition.START.ordinal) IconPosition.START else IconPosition.END
                    }
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.layout.setOnClickListener(l)
    }

    enum class IconPosition {
        START,
        END
    }
}