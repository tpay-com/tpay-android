package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import com.tpay.sdk.R
import com.tpay.sdk.extensions.isValidPostalCode

internal class TextFieldPostalCode @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextFieldAbstract(context, attrs, defStyleAttr) {
    init {
        setInputType(TextFieldInputType.NUMBER)
        setTextLength(6)
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? {
                return if (value.isValidPostalCode()) null else languageContext.getString(R.string.value_is_invalid)
            }
        })
        setInputFormatter(PostalCodeFormatter())
    }
}