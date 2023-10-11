package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import com.tpay.sdk.R
import com.tpay.sdk.extensions.isValidEmailAddress


internal class TextFieldEmail @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : TextFieldAbstract(context, attrs, defAttrStyle) {
    init {
        setInputType(TextFieldInputType.EMAIL)
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? {
                return if (value.isValidEmailAddress()) {
                    null
                } else {
                    languageContext.getString(R.string.email_address_not_valid)
                }
            }
        })
    }
}