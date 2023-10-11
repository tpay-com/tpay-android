package com.tpay.sdk.server.dto.request

import androidx.annotation.Keep
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.models.transaction.Frequency
import com.tpay.sdk.api.models.transaction.Frequency.Companion.code
import com.tpay.sdk.api.models.transaction.Quantity
import com.tpay.sdk.server.dto.parts.*
import org.json.JSONObject

@Keep
internal class CreateTransactionRequestDTO : JSONObject() {
    var amount: Double? = null
        set(value) {
            put("amount", value)
            field = value
        }
    var description: String? = null
        set(value) {
            put("description", value)
            field = value
        }
    private var lang: String? = null
        set(value) {
            put("lang", value)
            field = value
        }
    private var hiddenDescription: String? = null
        set(value) {
            put("hiddenDescription", value)
            field = value
        }
    var payer: PayerDTO? = null
        set(value) {
            put("payer", value)
            field = value
        }
    var pay: PayTransactionRequestDTO? = null
        set(value) {
            put("pay", value)
            field = value
        }
    private var callbacks: CallbacksDTO? = null
        set(value) {
            put("callbacks", value)
            field = value
        }

    fun applyRecursive(
        frequency: Frequency,
        quantity: Quantity,
        expirationDate: String
    ): CreateTransactionRequestDTO {
        return apply {
            pay?.recursive = RecursivePaymentDTO().apply {
                period = frequency.code
                if(quantity is Quantity.Specified){
                    this.quantity = quantity.value
                }
                type = TYPE_OF_RECURSIVE
                this.expirationDate = expirationDate
            }
        }
    }

    fun applyPayer(payer: Payer): CreateTransactionRequestDTO {
        return apply {
            this.payer = PayerDTO().apply {
                email = payer.email
                name = payer.name
                phone = payer.phone
                address = payer.address?.address
                city = payer.address?.city
                code = payer.address?.postalCode
                country = payer.address?.countryCode
            }
        }
    }

    fun applyPaymentDetails(
        amount: Double,
        description: String,
        hiddenDescription: String? = null,
        language: String? = null
    ): CreateTransactionRequestDTO {
        return apply {
            this.amount = amount
            this.description = description
            this.hiddenDescription = hiddenDescription
            this.lang = language
        }
    }

    fun applyBLIK(
        blikCode: String? = null,
        aliasValue: String? = null,
        aliasLabel: String? = null
    ): CreateTransactionRequestDTO {
        return apply {
            pay = PayTransactionRequestDTO().apply {
                groupId = BLIK_GROUP_ID
                blikPaymentData = BLIKPaymentDTO().apply {
                    blikCode?.let { blikToken = it }
                    aliasValue?.let {
                        aliases = AliasDTO().apply {
                            value = it
                            label = if (aliasLabel?.isBlank() == true) null else aliasLabel
                            type = BLIK_ALIAS_TYPE
                        }
                    }
                }
            }
        }
    }

    fun applyGooglePayToken(
        token: String
    ): CreateTransactionRequestDTO {
        return apply {
            pay = PayTransactionRequestDTO().apply {
                groupId = GOOGLE_PAY_GROUP_ID
                googlePayPaymentData = token
            }
        }
    }

    fun applyPayCard(
        encryptedCardData: String? = null,
        cardToken: String? = null,
        saveCard: Boolean? = null,
        roc: String? = null,
        payMethod: String = SALE_METHOD
    ): CreateTransactionRequestDTO {
        return apply {
            pay = PayTransactionRequestDTO().apply {
                groupId = PAY_CARD_GROUP_ID
                method = payMethod
                cardPaymentData = CardPaymentDTO().apply {
                    encryptedCardData?.run { card = this }
                    cardToken?.run { token = cardToken }
                    saveCard?.run { save = if(this) 1 else 0 }
                    roc?.run { rocText = this }
                }
            }
        }
    }

    fun applyTransfer(id: Int): CreateTransactionRequestDTO {
        return apply {
            pay = PayTransactionRequestDTO().apply {
                groupId = id
            }
        }
    }

    fun applyCallbacks(
        successUrl: String? = null,
        errorUrl: String? = null,
        notificationUrl: String? = null,
        notificationEmail: String? = null
    ): CreateTransactionRequestDTO = apply {
        callbacks = CallbacksDTO().apply {
            if(successUrl != null && errorUrl != null){
                payerUrls = PayerUrlDTO().apply {
                    success = successUrl
                    error = errorUrl
                }
            }
            if(notificationUrl != null || notificationEmail != null){
                notification = NotificationDTO().apply {
                    notificationUrl?.let { url = it }
                    notificationEmail?.let { email = it }
                }
            }
        }
    }

    companion object {
        private const val PAY_CARD_GROUP_ID = 103
        const val BLIK_GROUP_ID = 150
        private const val TYPE_OF_RECURSIVE = 1
        private const val GOOGLE_PAY_GROUP_ID = 166
        private const val SALE_METHOD = "sale"
        const val BLIK_ALIAS_TYPE = "UID"
    }
}