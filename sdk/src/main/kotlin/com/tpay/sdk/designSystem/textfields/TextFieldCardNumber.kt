package com.tpay.sdk.designSystem.textfields

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.tpay.sdk.R
import com.tpay.sdk.extensions.isValidCreditCardNumber
import com.tpay.sdk.extensions.px


internal class TextFieldCardNumber @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : TextFieldAbstract(context, attrs, defAttrStyle) {
    init {
        setInputType(TextFieldInputType.NUMBER)
        setTextLength(23)
        setInputFormatter(NumberSpacingFormatter(' ', 4))
        setOnTextChangedListener(object : OnTextChangedListener {
            override fun onChange(value: String) {
                val cardDrawableId = when (checkCreditCardType(value)) {
                    CreditCard.VISA -> R.drawable.ic_visa_16
                    CreditCard.MASTERCARD -> R.drawable.ic_mastercard_16
                    CreditCard.NONE -> null
                }

                binding.run {
                    if (cardDrawableId != null) {
                        cardIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                cardDrawableId
                            )
                        )
                        textField.setPadding(
                            TEXT_FIELD_PADDING_START,
                            0,
                            TEXT_FIELD_PADDING_END_WITH_CARD,
                            0.px
                        )
                    } else {
                        cardIcon.setImageDrawable(null)
                        textField.setPadding(
                            TEXT_FIELD_PADDING_START,
                            0,
                            TextFieldIcons.ALL.textFieldPaddingEnd,
                            0.px
                        )
                    }
                }
            }
        })
        setInputValidator(object : InputValidator {
            override fun validate(value: String): String? {
                val isCreditCardNumberValid = value.isValidCreditCardNumber()

                if (!isCreditCardNumberValid) {
                    return languageContext.getString(R.string.card_number_not_valid)
                }

                return null
            }
        })
    }

    private fun checkCreditCardType(cardNumber: String): CreditCard {
        if (cardNumber.isBlank()) {
            return CreditCard.NONE
        }
        CreditCard.values().forEach { creditCard ->
            if (cardNumber.matches(Regex(creditCard.regex))) {
                return creditCard
            }
        }

        return CreditCard.NONE
    }

    enum class CreditCard(val regex: String) {
        VISA(Validators.VISA_REGEX),
        MASTERCARD(Validators.MASTERCARD_REGEX),
        NONE("")
    }

    companion object {
        private val TEXT_FIELD_PADDING_START = 15.px
        private val TEXT_FIELD_PADDING_END_WITH_CARD = 125.px
    }
}