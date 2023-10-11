package com.tpay.sdk.internal.nfcScanner.extensions

import com.tpay.sdk.internal.nfcScanner.models.EMVTag
import com.tpay.sdk.internal.nfcScanner.models.TagWithData
import java.nio.ByteBuffer

internal fun ByteArray.getDataForTags(emvTags: List<EMVTag>): List<TagWithData> {
    val tagsWithData = mutableListOf<TagWithData>()

    emvTags.forEach { emvTag ->
        try {
            val indexOfTag = this.findTagIndexOrNull(emvTag)
            indexOfTag?.let { index ->
                val temp = this.copyOfRange(index + emvTag.tagBytes.size, this.size - 1)
                val lengthIndicator = temp.first().getBit(8)
                if(lengthIndicator == 0){
                    val data = temp.copyOfRange(1, 1 + temp.first().toInt())
                    tagsWithData.add(TagWithData(emvTag, data))
                } else {
                    val numberOfLengthBytes = temp.first().toInt() and 0b01111111
                    val tempWithoutLengthIndicator = temp.drop(1)
                    val lengthBytes = tempWithoutLengthIndicator.toByteArray().copyOfRange(0, numberOfLengthBytes)
                    val dataLength = when (lengthBytes.size) {
                        1 -> ByteBuffer.wrap(byteArrayOf(0x00, 0x00, 0x00) + lengthBytes).int
                        2 -> ByteBuffer.wrap(byteArrayOf(0x00, 0x00) + lengthBytes).int
                        else -> ByteBuffer.wrap(byteArrayOf(0x00) + lengthBytes).int
                    }
                    val data = tempWithoutLengthIndicator.toByteArray().copyOfRange(lengthBytes.size, dataLength)
                    tagsWithData.add(TagWithData(emvTag, data))
                }
            }
        } catch (exception: Exception){ }
    }

    return tagsWithData
}

internal fun ByteArray.getPDOLTags(): List<EMVTag> {
    val foundTags = mutableListOf<Pair<Int, EMVTag>>()

    EMVTag.pdolTags.forEach { pdolTag ->
        try {
            val indexOfTag = this.findTagIndexOrNull(pdolTag)

            indexOfTag?.let { index ->
                foundTags.add(index to pdolTag)
            }
        } catch (exception: Exception) { }
    }

    return foundTags.run {
        sortBy { it.first }
        map { it.second }
    }
}

internal fun ByteArray.findTagIndexOrNull(emvTag: EMVTag): Int? {
    this.toList().windowed(emvTag.tagBytes.size).forEachIndexed { index, windowedBytes ->
        if(emvTag.tagBytes.contentEquals(windowedBytes.toByteArray())) return index
    }
    return null
}

internal val ByteArray.hex
    get() = this.fold(""){ acc, byte ->
        acc + String.format("%02X", byte)
    }