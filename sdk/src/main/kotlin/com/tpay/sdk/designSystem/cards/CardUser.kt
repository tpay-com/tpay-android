package com.tpay.sdk.designSystem.cards

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tpay.sdk.R
import com.tpay.sdk.databinding.CardUserBinding


internal class CardUser @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = CardUserBinding.inflate(LayoutInflater.from(context), this, true)

    var userName: String
        get() = binding.name.text.toString()
        set(value) {
            binding.name.text = value
        }

    var userEmail: String
        get() = binding.email.text.toString()
        set(value) {
            binding.email.text = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.CardUser).apply {
                try {
                    userName = getString(R.styleable.CardUser_userName) ?: ""
                    userEmail = getString(R.styleable.CardUser_userEmail) ?: ""
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.layout.setOnClickListener(l)
    }
}