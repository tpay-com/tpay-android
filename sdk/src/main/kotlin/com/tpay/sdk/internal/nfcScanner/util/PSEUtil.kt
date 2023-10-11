package com.tpay.sdk.internal.nfcScanner.util

import com.tpay.sdk.internal.nfcScanner.commands.CardType
import com.tpay.sdk.internal.nfcScanner.commands.PSECommand
import com.tpay.sdk.internal.nfcScanner.models.PSEResponse

internal class PSEUtil {
    companion object {
        internal fun getPSECommand(cardType: CardType): ByteArray = PSECommand(cardType).getBytes()
        internal fun getPSEResponse(responseBytes: ByteArray): PSEResponse = PSEResponse.from(responseBytes)
    }
}