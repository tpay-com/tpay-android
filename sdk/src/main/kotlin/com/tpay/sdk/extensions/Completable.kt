@file:Suppress("unused")

package com.tpay.sdk.extensions

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal fun <T> completable(body: (Completable<T>) -> Unit): Completable<T> {
    return Completable.create { completable ->  body(completable) }
}

internal class CompletableImpl <T> (private val block: (Completable<T>) -> Unit) : Completable<T> {
    override var successObserver: ((T) -> Unit)? = null
    override var errorObserver: ((Exception) -> Unit)? = null
    override var observeOn: Threads = Threads.MAIN
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun observe(onSuccess: (T) -> Unit, onError: (Exception) -> Unit): Completable<T> {
        successObserver = onSuccess
        errorObserver = onError

        block.invoke(this)
        return this
    }

    override fun onSuccess(value: T) {
        executeDependingOnThread(observeOn) {
            successObserver?.invoke(value)
            dispose()
        }
    }

    override fun onError(value: Exception) {
        executeDependingOnThread(observeOn){
            errorObserver?.invoke(value)
            dispose()
        }
    }

    override fun observeOn(thread: Threads): Completable<T> {
        observeOn = thread
        return this
    }

    private fun executeDependingOnThread(thread: Threads, block: () -> Unit){
        when(thread){
            Threads.IO -> {
                executor.execute(block)
            }
            Threads.MAIN -> {
                runOnMainThread(block)
            }
        }
    }

    override fun dispose() {
        executor.shutdown()
        successObserver = null
        errorObserver = null
    }

    override fun disposedBy(disposeBag: DisposeBag) {
        disposeBag.add(this)
    }
}

internal interface Disposable {
    fun dispose()
    fun disposedBy(disposeBag: DisposeBag)
}

internal interface Completable <T> : Disposable {
    var successObserver: ((T) -> Unit)?
    var errorObserver: ((Exception) -> Unit)?
    var observeOn: Threads

    fun observe(
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit
    ): Completable<T>
    fun onSuccess(value: T)
    fun onError(value: Exception)
    fun observeOn(thread: Threads): Completable<T>

    companion object {
        fun <T> create(block: (Completable<T>) -> Unit): Completable<T> {
            return CompletableImpl(block)
        }
    }
}

internal enum class Threads {
    MAIN, IO
}
