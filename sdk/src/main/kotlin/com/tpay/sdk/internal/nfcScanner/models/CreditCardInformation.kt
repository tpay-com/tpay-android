package com.tpay.sdk.internal.nfcScanner.models

import com.tpay.sdk.internal.nfcScanner.extensions.containsAll
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTag
import com.tpay.sdk.internal.nfcScanner.extensions.getDataForTags
import com.tpay.sdk.internal.nfcScanner.extensions.hex
import java.util.*


internal data class CreditCardInformation(
    val cardNumber: String,
    val expirationDate: Date
) {
    companion object {
        private val identifyingTags = listOf(
            EMVTag.APPLICATION_PRIMARY_ACCOUNT_NUMBER,
            EMVTag.APPLICATION_EXPIRATION_DATE,
            EMVTag.ISSUER_COUNTRY_CODE
        )

        internal fun from(records: List<ByteArray>): CreditCardInformation? {
            var tagsWithData = listOf<TagWithData>()

            records.forEach { record ->
                val tempTagsWithData = record.getDataForTags(identifyingTags)
                if(tempTagsWithData.containsAll(identifyingTags)){
                    tagsWithData = tempTagsWithData
                }
            }

            if(!tagsWithData.containsAll(identifyingTags) || tagsWithData.isEmpty()) return null

            val accountNumber = tagsWithData.getDataForTag(EMVTag.APPLICATION_PRIMARY_ACCOUNT_NUMBER).hex
            val expirationDate = tagsWithData.getDataForTag(EMVTag.APPLICATION_EXPIRATION_DATE).hex
            val expirationDateChunked = expirationDate.chunked(2)

            return try {
                CreditCardInformation(
                    cardNumber = accountNumber,
                    expirationDate = GregorianCalendar(
                        "20${expirationDateChunked.first()}".toInt(),
                        expirationDateChunked[1].toInt() - 1,
                        expirationDateChunked.last().toInt()
                    ).time
                )
            } catch (exception: NumberFormatException) {
                null
            }
        }
    }
}
