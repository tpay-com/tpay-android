package com.tpay.sdk.designSystem.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ButtonLinkBinding

internal class ButtonLink @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ButtonLinkBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String
        get() = binding.buttonText.text.toString()
        set(value) {
            binding.buttonText.text = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ButtonLink).apply {
                try {
                    text = getString(R.styleable.ButtonLink_buttonText) ?: ""
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.buttonText.setOnClickListener(l)
    }
}