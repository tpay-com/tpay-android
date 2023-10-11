package com.tpay.sdk.internal.nfcScanner.util

import com.tpay.sdk.internal.nfcScanner.models.SelectApplicationResponse
import com.tpay.sdk.internal.nfcScanner.commands.SelectApplicationCommand

internal class SelectApplicationUtil {
    companion object {
        internal fun getSelectApplicationCommand(applicationId: ByteArray): ByteArray {
            return SelectApplicationCommand(applicationId).getBytes()
        }
        internal fun getSelectApplicationResponse(responseBytes: ByteArray): SelectApplicationResponse? {
            return SelectApplicationResponse.from(responseBytes)
        }
    }
}