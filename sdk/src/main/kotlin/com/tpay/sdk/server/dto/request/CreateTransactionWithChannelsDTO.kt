package com.tpay.sdk.server.dto.request

import androidx.annotation.Keep
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.models.transaction.Frequency
import com.tpay.sdk.api.models.transaction.Frequency.Companion.code
import com.tpay.sdk.api.models.transaction.Quantity
import com.tpay.sdk.server.dto.parts.AliasDTO
import com.tpay.sdk.server.dto.parts.BLIKPaymentDTO
import com.tpay.sdk.server.dto.parts.CallbacksDTO
import com.tpay.sdk.server.dto.parts.CardPaymentDTO
import com.tpay.sdk.server.dto.parts.NotificationDTO
import com.tpay.sdk.server.dto.parts.PayerDTO
import com.tpay.sdk.server.dto.parts.PayerUrlDTO
import com.tpay.sdk.server.dto.parts.RecursivePaymentDTO
import org.json.JSONObject

@Keep
internal class CreateTransactionWithChannelsDTO : JSONObject() {
    var amount: Double? = null
        set(value) {
            put(AMOUNT, value)
            field = value
        }

    var description: String? = null
        set(value) {
            put(DESCRIPTION, value)
            field = value
        }

    private var lang: String? = null
        set(value) {
            put(LANG, value)
            field = value
        }

    private var hiddenDescription: String? = null
        set(value) {
            put(HIDDEN_DESCRIPTION, value)
            field = value
        }

    var payer: PayerDTO? = null
        set(value) {
            put(PAYER, value)
            field = value
        }

    var pay: PayWithRedirectionRequestDTO? = null
        set(value) {
            put(PAY, value)
            field = value
        }

    private var callbacks: CallbacksDTO? = null
        set(value) {
            put(CALLBACKS, value)
            field = value
        }

    fun applyRecursive(
        frequency: Frequency,
        quantity: Quantity,
        expirationDate: String
    ): CreateTransactionWithChannelsDTO {
        return apply {
            pay?.recursive = RecursivePaymentDTO().apply {
                period = frequency.code
                if (quantity is Quantity.Specified){
                    this.quantity = quantity.value
                }
                type = TYPE_OF_RECURSIVE
                this.expirationDate = expirationDate
            }
        }
    }

    fun applyPayer(payer: Payer): CreateTransactionWithChannelsDTO {
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
    ): CreateTransactionWithChannelsDTO {
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
    ): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = BLIK_CHANNEL_ID
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

    fun applyGooglePayToken(token: String): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = GOOGLE_PAY_CHANNEL_ID
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
    ): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = CREDIT_CARD_CHANNEL_ID
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

    fun applyPayPo(): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = PAY_PO_CHANNEL_ID
            }
        }
    }

    fun applyTransfer(id: Int): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = id
            }
        }
    }

    fun applyPekaoInstallment(id: Int): CreateTransactionWithChannelsDTO {
        return apply {
            pay = PayWithRedirectionRequestDTO().apply {
                channelId = id
            }
        }
    }

    fun applyCallbacks(
        successUrl: String? = null,
        errorUrl: String? = null,
        notificationUrl: String? = null,
        notificationEmail: String? = null
    ): CreateTransactionWithChannelsDTO = apply {
        callbacks = CallbacksDTO().apply {
            if (successUrl != null && errorUrl != null){
                payerUrls = PayerUrlDTO().apply {
                    success = successUrl
                    error = errorUrl
                }
            }
            if (notificationUrl != null || notificationEmail != null){
                notification = NotificationDTO().apply {
                    notificationUrl?.let { url = it }
                    notificationEmail?.let { email = it }
                }
            }
        }
    }

    companion object {
        private const val CREDIT_CARD_CHANNEL_ID = 53
        private const val BLIK_CHANNEL_ID = 64
        private const val GOOGLE_PAY_CHANNEL_ID = 68
        private const val PAY_PO_CHANNEL_ID = 83
        private const val TYPE_OF_RECURSIVE = 1
        private const val SALE_METHOD = "sale"
        private const val BLIK_ALIAS_TYPE = "UID"

        private const val AMOUNT = "amount"
        private const val DESCRIPTION = "description"
        private const val LANG = "lang"
        private const val HIDDEN_DESCRIPTION = "hiddenDescription"
        private const val PAYER = "payer"
        private const val PAY = "pay"
        private const val CALLBACKS = "callbacks"
    }
}