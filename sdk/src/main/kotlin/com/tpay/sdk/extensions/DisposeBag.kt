package com.tpay.sdk.extensions

import javax.inject.Singleton

@Singleton
internal class DisposeBag {
    private val disposables = mutableListOf<Disposable>()

    fun add(disposable: Disposable){
        disposables.add(disposable)
    }

    fun disposeAll(){
        disposables.forEach { disposable ->
            disposable.dispose()
        }
        disposables.clear()
    }
}