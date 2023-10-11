package com.tpay.sdk.internal

import javax.inject.Singleton

@Singleton
internal data class AddCardCoordinator(
    var addCardSuccess: (String?) -> Unit = { },
    var addCardFailure: () -> Unit = { },
    var moduleClosed: () -> Unit = { }
)