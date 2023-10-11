package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import com.tpay.sdk.R
import com.tpay.sdk.extensions.focusAndShowKeyboard
import java.util.*


internal class TextFieldCardDate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : TextFieldAbstract(context, attrs, defAttrStyle) {
    init {
        setInputType(TextFieldInputType.NUMBER)
        setTextLength(5)
        setFocusListener(object : FocusListener {
            override fun onChange(hasFocus: Boolean, isEmpty: Boolean) {
                binding.textField.run {
                    if (hasFocus && isEmpty) {
                        hint = languageContext.getString(R.string.credit_card_expiration_date_format)
                        focusAndShowKeyboard()
                    } else hint = ""
                }
            }
        })
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? =
                if (CreditCardDate.from(value)?.isValid() == true) null
                else languageContext.getString(R.string.credit_card_date_not_valid)
        })
        setInputFormatter(ExpirationDateFormatter())
    }
}

internal data class CreditCardDate(val month: Int, val year: Int) {
    private val calendar = Calendar.getInstance()

    companion object {
        internal fun from(value: String): CreditCardDate? {
            return try {
                val month = value.substringBefore('/').toInt()
                if (month < 1 || month > 12) return null
                val year2Digits = value.substringAfter('/')
                if (year2Digits.isBlank()) return null
                val year = "20$year2Digits".toInt()
                if (year < 0) return null
                CreditCardDate(month, year)
            } catch (exception: NumberFormatException) {
                null
            }
        }
    }

    fun isValid(): Boolean {
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        if (currentYear > year) {
            return false
        }
        if (currentYear == year && currentMonth > month) {
            return false
        }

        return true
    }
}