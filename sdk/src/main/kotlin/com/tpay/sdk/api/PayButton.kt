package com.tpay.sdk

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tpay.sdk.databinding.ButtonPayBinding

/**
 * Button that can be used to represent Tpay as a payment option
 */
class PayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ButtonPayBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }
}