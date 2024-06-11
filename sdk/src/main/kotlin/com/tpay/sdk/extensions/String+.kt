package com.tpay.sdk.extensions

import android.text.TextUtils
import com.tpay.sdk.designSystem.textfields.Validators
import java.util.regex.Matcher
import java.util.regex.Pattern

internal fun String.fromPEMPublicKey(): String {
    return replace("-----BEGIN PUBLIC KEY-----", "")
        .replace(System.lineSeparator(), "")
        .replace("-----END PUBLIC KEY-----", "")
}

internal fun String.isValidEmailAddress(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        val matcher: Matcher =
            Pattern.compile("(?:[a-zA-Z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")
                .matcher(this)
        return matcher.matches()
    }
}

internal fun String.isValidBLIKCode(): Boolean {
    return this.matches(Regex("^[0-9]{6}$"))
}

internal fun String.isValidPostalCode(): Boolean {
    return matches(Regex("^[0-9]{2}-[0-9]{3}$"))
}

internal fun String.isValidCreditCardNumber(): Boolean {
    return matches(Regex(Validators.MASTERCARD_REGEX)) || matches(Regex(Validators.VISA_REGEX))
}

internal fun String.isValidCVVCode(): Boolean {
    return matches(Regex(Validators.CVV_REGEX))
}

internal fun String.isValidFirstAndLastName(): Boolean {
    if (isBlank()) return false
    if (containsEmoji()) return false
    return !Regex("[~!@#$%^&*()_+]").containsMatchIn(this)
}

internal const val ADDRESS_AND_CITY_REGEX = "[~!@#$%^*()_+]"

internal fun String.isValidAddress(): Boolean {
    if (isBlank()) return false
    if (containsEmoji()) return false
    return !Regex(ADDRESS_AND_CITY_REGEX).containsMatchIn(this)
}

internal fun String.isValidCity(): Boolean {
    if (isBlank()) return false
    if (containsEmoji()) return false
    return !Regex(ADDRESS_AND_CITY_REGEX).containsMatchIn(this)
}

internal fun String.containsEmoji(): Boolean {
    return Pattern.compile("\\p{So}+", Pattern.CASE_INSENSITIVE).matcher(this).find()
}

internal val String.isFirstAndLastNameLengthValid: Boolean
    get() = length in (3..255)

internal val String.isAddressLengthValid: Boolean
    get() = length in (3..255)

internal val String.isCityLengthValid: Boolean
    get() = length in (1..255)

internal fun String.addAtEndIfNotThere(text: String): String {
    return if (!this.endsWith(text)) {
        "$this$text"
    } else {
        this
    }
}