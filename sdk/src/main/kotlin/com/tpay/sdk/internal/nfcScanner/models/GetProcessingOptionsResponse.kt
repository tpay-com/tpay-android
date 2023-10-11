package com.tpay.sdk.internal.nfcScanner.models

import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidCreditCardResponseException
import com.tpay.sdk.internal.nfcScanner.exceptions.InvalidEMVDataFormatException
import com.tpay.sdk.internal.nfcScanner.extensions.containsAll
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTag
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTags
import com.tpay.sdk.internal.nfcScanner.extensions.hex
import com.tpay.sdk.internal.nfcScanner.util.ErrorMessages


internal data class GetProcessingOptionsResponse(
    val aflRecords: List<AFLRecord>
) {
    companion object {
        private val identifyingTags = listOf(
            EMVTag.RESPONSE_MESSAGE_TEMPLATE_FORMAT_2,
            EMVTag.APPLICATION_FILE_LOCATOR
        )

        internal fun from(value: ByteArray): GetProcessingOptionsResponse {
            if(value.size < 3) throw InvalidCreditCardResponseException(ErrorMessages.PAY_CARD_RETURNED_ERROR + " ${value.hex}")

            val tagsWithData = value.getDataForTags(identifyingTags)
            if(!tagsWithData.containsAll(identifyingTags)){
                throw InvalidEMVDataFormatException(ErrorMessages.INVALID_DATA_FORMAT + " get processing response")
            }

            val fileLocatorRecords = tagsWithData.getDataForTag(EMVTag.APPLICATION_FILE_LOCATOR)
            val tempRecords = mutableListOf<AFLRecord>()
            fileLocatorRecords.toList().chunked(4).forEach {
                if(it[1] > 0 && it[2] > 0){
                    tempRecords.add(
                        AFLRecord(
                            sfi = (it[0].toInt().shr(3).shl(3) or 4).toByte(),
                            startIndex = it[1],
                            endIndex = it[2]
                        )
                    )
                }
            }

            return GetProcessingOptionsResponse(tempRecords)
        }
    }
}