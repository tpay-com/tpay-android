package com.tpay.sdk.internal.nfcScanner.models

import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidCreditCardResponseException
import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidEMVDataFormatException
import com.tpay.sdk.internal.nfcScanner.extensions.containsAll
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTag
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTags
import com.tpay.sdk.internal.nfcScanner.extensions.hex
import com.tpay.sdk.internal.nfcScanner.util.ErrorMessages


internal data class PSEResponse(
    val applicationId: ByteArray
) {
    companion object {
        private val identifyingTags = listOf(
            EMVTag.FILE_CONTROL_INFORMATION_TEMPLATE,
            EMVTag.APPLICATION_IDENTIFIER
        )

        internal fun from(value: ByteArray): PSEResponse {
            if (value.size < 3) throw InvalidCreditCardResponseException(ErrorMessages.PAY_CARD_RETURNED_ERROR + " ${value.hex}")
            val tagsWithData = value.getDataForTags(identifyingTags)

            if(!tagsWithData.containsAll(identifyingTags)){
                throw InvalidEMVDataFormatException(ErrorMessages.INVALID_DATA_FORMAT + " pse response")
            }

            return PSEResponse(tagsWithData.getDataForTag(EMVTag.APPLICATION_IDENTIFIER))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PSEResponse

        if (!applicationId.contentEquals(other.applicationId)) return false

        return true
    }

    override fun hashCode(): Int {
        return applicationId.contentHashCode()
    }
}