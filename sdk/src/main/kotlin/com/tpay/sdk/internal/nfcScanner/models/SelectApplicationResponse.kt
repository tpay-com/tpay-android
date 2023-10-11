package com.tpay.sdk.internal.nfcScanner.models

import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidCreditCardResponseException
import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidEMVDataFormatException
import com.tpay.sdk.internal.nfcScanner.extensions.*
import com.tpay.sdk.internal.nfcScanner.util.ErrorMessages


internal data class SelectApplicationResponse(
    val pdol: ByteArray,
    val pdolTags: List<EMVTag>
) {
    companion object {
        private val identifyingTags = listOf(EMVTag.APPLICATION_LABEL)
        internal fun from(value: ByteArray): SelectApplicationResponse? {
            if (value.size < 3) throw InvalidCreditCardResponseException(ErrorMessages.PAY_CARD_RETURNED_ERROR + " ${value.hex}")

            val tagsAndData = value.getDataForTags(identifyingTags + EMVTag.PROCESSING_OPTIONS_DATA_OBJECT_LIST)
            if(!tagsAndData.containsAll(identifyingTags)){
                throw InvalidEMVDataFormatException(ErrorMessages.INVALID_DATA_FORMAT + " select application response")
            }

            return try {
                val data = tagsAndData.getDataForTag(EMVTag.PROCESSING_OPTIONS_DATA_OBJECT_LIST)
                SelectApplicationResponse(pdol = data, pdolTags = data.getPDOLTags())
            } catch (exception: Exception){
                null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectApplicationResponse

        if (!pdol.contentEquals(other.pdol)) return false

        return true
    }

    override fun hashCode(): Int {
        return pdol.contentHashCode()
    }
}
