package com.tpay.sdk.internal.nfcScanner.models


internal data class TagWithData(
    val tag: EMVTag,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagWithData

        if (tag != other.tag) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
