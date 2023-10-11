package com.tpay.sdk.api.addCard

interface AddCardDelegate {
    /**
     * Called when tokenization was successful
     */
    fun onAddCardSuccess(tokenizationId: String?)

    /**
     * Called when tokenization failed
     */
    fun onAddCardFailure()

    /**
     * Called when module was closed
     */
    fun onModuleClosed()
}