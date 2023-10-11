package com.tpay.sdk.internal

import com.tpay.sdk.extensions.Completable
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


internal class CompletableScheduler<T>(private val completableProvider: () -> Completable<T>) {
    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var shouldRun = true

    fun schedule(
        everyMillis: Long,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit
    ) {
        ioExecutor.execute {
            while (shouldRun) {
                completableProvider.invoke().run {
                    observe(onSuccess, onError)

                    Thread.sleep(everyMillis)
                    dispose()
                }
            }
            ioExecutor.shutdown()
        }
    }

    fun stop() {
        shouldRun = false
    }
}