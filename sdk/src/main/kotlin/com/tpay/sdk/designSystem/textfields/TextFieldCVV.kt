package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import com.tpay.sdk.R
import com.tpay.sdk.extensions.isValidCVVCode


internal class TextFieldCVV @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : TextFieldAbstract(context, attrs, defAttrStyle) {
    init {
        setInputType(TextFieldInputType.NUMBER)
        setTextLength(3)
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? {
                return if (value.isValidCVVCode()) {
                    null
                } else {
                    languageContext.getString(R.string.card_cvv_number_not_valid)
                }
            }
        })
    }
}