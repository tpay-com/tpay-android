package com.tpay.sdk.cache

import com.tpay.sdk.internal.nfcScanner.extensions.hex
import java.security.MessageDigest

internal class CachedNetworkImage(
    val urlHash: String,
    val bytes: ByteArray
) {
    companion object {
        fun from(url: String, bytes: ByteArray): CachedNetworkImage? {
            val hash = hashUrl(url)
            return if(hash != null && bytes.isNotEmpty()){
                CachedNetworkImage(hash, bytes)
            } else {
                null
            }
        }

        fun hashUrl(url: String): String? = url.hash

        private val String.hash: String?
            get() {
                return try {
                    val digest = MessageDigest.getInstance(HASH_ALGORITHM)
                    digest.digest(toByteArray()).hex
                } catch (exception: Exception){
                    null
                }
            }

        private const val HASH_ALGORITHM = "SHA-256"
    }
}