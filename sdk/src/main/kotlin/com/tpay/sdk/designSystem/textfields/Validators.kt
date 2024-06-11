package com.tpay.sdk.designSystem.textfields

import com.tpay.sdk.R
import com.tpay.sdk.extensions.isAddressLengthValid
import com.tpay.sdk.extensions.isCityLengthValid
import com.tpay.sdk.extensions.isFirstAndLastNameLengthValid
import com.tpay.sdk.extensions.isValidAddress
import com.tpay.sdk.extensions.isValidCity
import com.tpay.sdk.extensions.isValidEmailAddress
import com.tpay.sdk.extensions.isValidFirstAndLastName
import com.tpay.sdk.extensions.isValidPostalCode

internal object Validators {
    const val VISA_REGEX = "^4([0-9]{12}|[0-9]{15}|[0-9]{18})$"
    const val MASTERCARD_REGEX =
        "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$"
    const val CVV_REGEX = "^[0-9]{3}$"

    fun validatePayerAddress(value: String?): Int? {
        return when {
            value.isNullOrBlank() -> R.string.field_required
            !value.isAddressLengthValid -> R.string.invalid_number_of_characters
            !value.isValidAddress() -> R.string.value_is_invalid
            else -> null
        }
    }

    fun validatePayerCity(value: String?): Int? {
        return when {
            value.isNullOrBlank() -> R.string.field_required
            !value.isCityLengthValid -> R.string.invalid_number_of_characters
            !value.isValidCity() -> R.string.value_is_invalid
            else -> null
        }
    }

    fun validatePayerName(value: String): Int? {
        return when {
            value.isBlank() -> R.string.field_required
            !value.isFirstAndLastNameLengthValid -> R.string.invalid_number_of_characters
            !value.isValidFirstAndLastName() -> R.string.first_last_name_invalid
            else -> null
        }
    }

    fun validatePayerEmail(value: String): Int? {
        return when {
            value.isBlank() -> R.string.field_required
            !value.isValidEmailAddress() -> R.string.email_address_not_valid
            else -> null
        }
    }

    fun validatePayerPostalCode(value: String?): Int? {
        return when {
            value.isNullOrBlank() -> R.string.field_required
            value.isValidPostalCode() -> null
            else -> R.string.value_is_invalid
        }
    }
}