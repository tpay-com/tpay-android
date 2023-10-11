package com.tpay.sdk.designSystem.textfields


internal class Validators {
    companion object {
        const val VISA_REGEX = "^4([0-9]{12}|[0-9]{15}|[0-9]{18})$"
        const val MASTERCARD_REGEX =
            "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$"
        const val CVV_REGEX = "^[0-9]{3}$"
    }
}