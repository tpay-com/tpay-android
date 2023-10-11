package com.tpay.sdk.extensions

internal open class Observable<T>(initialValue: T? = null) {
    private var observer: ((T) -> Unit)? = null
    fun observe(observe: (T) -> Unit) {
        this.observer = observe
    }

    fun dispose() {
        this.observer = null
    }

    var value = initialValue
        set(value) {
            value?.let {
                field = it
                observer?.invoke(it)
            }
        }
}