package com.tpay.sdk.internal.nfcScanner.extensions

import com.tpay.sdk.internal.nfcScanner.models.EMVTag
import com.tpay.sdk.internal.nfcScanner.models.TagWithData

internal fun List<TagWithData>.containsAll(tags: List<EMVTag>) : Boolean {
    tags.forEach { emvTag ->
        if(none { it.tag == emvTag }) return false
    }
    return true
}

internal fun List<TagWithData>.getDataForTag(emvTag: EMVTag) : ByteArray {
    return this.first { it.tag == emvTag }.data
}