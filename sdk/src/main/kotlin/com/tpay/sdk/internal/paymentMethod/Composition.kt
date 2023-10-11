package com.tpay.sdk.internal.paymentMethod

import android.content.Context
import com.tpay.sdk.internal.FormError

internal abstract class Composition(private val context: Context) {
    abstract fun onCreate()
    abstract fun onDestroy()

    fun getStringOrNull(id: Int?): String? {
        if (id == null) return null
        return if (id != -1) {
            context.getString(id)
        } else null
    }

    fun getColor(id: Int) = context.getColor(id)

    fun getError(formError: FormError): String? {
        return if (formError is FormError.Resource) {
            context.getString(formError.id)
        } else null
    }
}

internal class CompositionManager {
    var currentComposition: Composition? = null

    fun changeIfDifferent(composition: Composition) {
        if (currentComposition?.javaClass != composition.javaClass) {
            currentComposition?.onDestroy()
            currentComposition = composition
            currentComposition?.onCreate()
        }
    }

    fun change(composition: Composition) {
        currentComposition?.onDestroy()
        currentComposition = composition
        currentComposition?.onCreate()
    }

    fun onDestroy() {
        currentComposition?.onDestroy()
        currentComposition = null
    }
}