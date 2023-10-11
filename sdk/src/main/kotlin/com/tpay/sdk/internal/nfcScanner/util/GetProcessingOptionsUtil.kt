package com.tpay.sdk.internal.nfcScanner.util

import com.tpay.sdk.internal.nfcScanner.models.EMVTag
import com.tpay.sdk.internal.nfcScanner.models.GetProcessingOptionsResponse
import com.tpay.sdk.internal.nfcScanner.commands.GetProcessingOptionsCommand
import com.tpay.sdk.internal.nfcScanner.commands.GetProcessingOptionsWithPDOLCommand

internal class GetProcessingOptionsUtil {
    companion object {
        internal fun getProcessingOptionsCommand(): ByteArray = GetProcessingOptionsCommand().getBytes()
        internal fun getProcessingOptionsWithPDOLCommand(pdolTags: List<EMVTag>): ByteArray {
            return GetProcessingOptionsWithPDOLCommand(pdolTags).getBytes()
        }
        internal fun getProcessingOptionsResponse(responseBytes: ByteArray): GetProcessingOptionsResponse {
            return GetProcessingOptionsResponse.from(responseBytes)
        }
    }
}