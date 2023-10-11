package com.tpay.sdk.extensions

import android.text.InputFilter
import android.widget.EditText

internal fun EditText.addFilter(filter: InputFilter) {
    filters =
        if (filters.isNullOrEmpty()) {
            arrayOf(filter)
        } else {
            filters
                .toMutableList()
                .apply {
                    removeAll { it.javaClass == filter.javaClass }
                    add(filter)
                }
                .toTypedArray()
        }
}