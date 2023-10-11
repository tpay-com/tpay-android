package com.tpay.sdk.internal

import androidx.annotation.StringRes

internal sealed class FormError {
    internal data class Resource(@StringRes val id: Int) : FormError()
    internal object None : FormError()
}