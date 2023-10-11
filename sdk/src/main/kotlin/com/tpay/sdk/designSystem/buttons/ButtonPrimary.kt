package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonPrimaryBinding
import com.tpay.sdk.extensions.isVisible


internal class ButtonPrimary @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        ButtonPrimaryBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String = ""
        set(value) {
            binding.button.text = value
            field = value
        }

    var isLoading: Boolean = false
        set(value) {
            binding.run {
                button.text = if (value) "" else text
                button.isClickable = !value
                progress.isVisible = value
            }
            field = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ButtonPrimary).apply {
                try {
                    text = getString(R.styleable.ButtonPrimary_buttonText) ?: ""
                    isLoading = getBoolean(R.styleable.ButtonPrimary_loading, false)
                    isEnabled = getBoolean(R.styleable.ButtonPrimary_enabled, true)
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.button.setOnClickListener(l)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.button.run {
            isEnabled = enabled
            isClickable = enabled
        }
    }
}