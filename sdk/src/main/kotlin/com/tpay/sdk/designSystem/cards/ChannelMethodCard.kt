@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.cards

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.tpay.sdk.R
import com.tpay.sdk.databinding.ChannelMethodCardBinding
import com.tpay.sdk.extensions.px


internal class ChannelMethodCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : FrameLayout(context, attrs, defAttrStyle) {
    private val binding = ChannelMethodCardBinding.inflate(LayoutInflater.from(context), this, true)

    var name: String
        get() = binding.methodName.text.toString()
        set(value) {
            binding.methodName.text = value
        }

    var logo: Drawable?
        get() = binding.methodLogo.drawable
        set(value) {
            binding.run {
                methodLogo.setImageDrawable(value)
                methodName.isVisible = value == null
            }
        }

    var isNameVisible: Boolean
        get() = binding.methodName.isVisible
        set(value) {
            binding.methodName.isVisible = value
        }

    var type: Type = Type.TRANSFER
        set(value) {
            binding.methodLogo.run {
                updatePadding(top = value.verticalPadding, bottom = value.verticalPadding)
            }
            field = value
        }

    var cardState: ChannelMethodCardState = ChannelMethodCardState.ENABLED
        set(value) {
            binding.root.background = ContextCompat.getDrawable(context, value.backgroundId)
            field = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.ChannelMethodCard).apply {
                logo = getDrawable(R.styleable.ChannelMethodCard_methodLogo)
                name = getString(R.styleable.ChannelMethodCard_methodName) ?: ""
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        binding.root.isSelected = selected
        cardState = if (selected) ChannelMethodCardState.CHECKED else ChannelMethodCardState.ENABLED
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    enum class ChannelMethodCardState(val backgroundId: Int) {
        ENABLED(R.drawable.bank_card_enabled),
        CHECKED(R.drawable.bank_card_checked)
    }

    enum class Type(val verticalPadding: Int) {
        TRANSFER(0.px), RATY_PEKAO(24.px)
    }
}