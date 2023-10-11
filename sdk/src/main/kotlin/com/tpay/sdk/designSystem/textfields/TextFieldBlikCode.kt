package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import com.tpay.sdk.R
import com.tpay.sdk.extensions.isValidBLIKCode


internal class TextFieldBlikCode @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextFieldAbstract(context, attrs, defStyleAttr) {
    init {
        setInputType(TextFieldInputType.NUMBER)
        setTextLength(7)
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? =
                if (value.isValidBLIKCode()) null else languageContext.getString(R.string.blik_code_not_valid)
        })
        setInputFormatter(NumberSpacingFormatter(' ', 3))
    }
}