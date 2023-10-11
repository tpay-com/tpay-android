package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet


internal class TextFieldStandard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : TextFieldAbstract(context, attrs, defAttrStyle) {
    init {
        setInputType(TextFieldInputType.TEXT)
    }
}