package com.tpay.sdk.extensions

import androidx.lifecycle.SavedStateHandle

inline fun <reified T> SavedStateHandle.getOrThrow(key: String): T =
    get(key) ?: throw IllegalStateException()