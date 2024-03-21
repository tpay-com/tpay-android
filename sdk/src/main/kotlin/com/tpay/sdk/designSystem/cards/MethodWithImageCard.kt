@file:Suppress("MemberVisibilityCanBePrivate")

package com.tpay.sdk.designSystem.cards

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.tpay.sdk.R
import com.tpay.sdk.databinding.MethodWithImageCardBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.px


internal class MethodWithImageCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : FrameLayout(context, attrs, defAttrStyle) {
    private val binding = MethodWithImageCardBinding.inflate(LayoutInflater.from(context), this, true)

    var name: String
        get() = binding.methodName.text.toString()
        set(value) {
            binding.methodName.run {
                text = value
                isVisible = logo == null
            }
        }

    var logo: Drawable?
        get() = binding.methodLogo.drawable
        set(value) {
            binding.run {
                methodLogo.setImageDrawable(value)
                methodName.isVisible = value == null
            }
        }

    var type: Type = Type.TRANSFER
        set(value) {
            binding.methodLogo.run {
                updatePadding(top = value.verticalPadding, bottom = value.verticalPadding)
            }
            field = value
        }

    var cardState: MethodWithImageCardState = MethodWithImageCardState.ENABLED
        set(value) {
            binding.root.background = ContextCompat.getDrawable(context, value.backgroundId)
            field = value
        }

    init {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.MethodWithImageCard).apply {
                logo = getDrawable(R.styleable.MethodWithImageCard_methodLogo)
                name = getString(R.styleable.MethodWithImageCard_methodName) ?: ""
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        binding.root.isSelected = selected
        cardState = if (selected) MethodWithImageCardState.CHECKED else MethodWithImageCardState.ENABLED
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }

    enum class MethodWithImageCardState(val backgroundId: Int) {
        ENABLED(R.drawable.bank_card_enabled),
        CHECKED(R.drawable.bank_card_checked)
    }

    enum class Type(val verticalPadding: Int) {
        TRANSFER(0.px), RATY_PEKAO(36.px)
    }
}