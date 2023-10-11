package com.tpay.sdk.api

import android.util.Base64
import com.tpay.sdk.extensions.fromPEMPublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * Class responsible for encrypting credit card information
 */
class PayCardEncryptor(private val publicKeyString: String) {
    fun encrypt(
        cardNumber: String,
        expirationDate: String,
        cvv: String,
        domain: String
    ): String {
        val stringKey = publicKeyString.fromPEMPublicKey()

        val encoded = Base64.decode(stringKey, Base64.DEFAULT)

        val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        val keySpec = X509EncodedKeySpec(encoded)
        val publicKey = keyFactory.generatePublic(keySpec)

        val rsa = Cipher.getInstance(CIPHER_TRANSFORMATION)
        rsa.init(Cipher.ENCRYPT_MODE, publicKey)

        val cardDataBytes = "$cardNumber|$expirationDate|$cvv|$domain".toByteArray()

        val encrypted = rsa.doFinal(cardDataBytes)

        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    companion object {
        const val KEY_FACTORY_ALGORITHM = "RSA"
        const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    }
}