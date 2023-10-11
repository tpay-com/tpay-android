package com.tpay.sdk.api.screenless.googlePay

import org.json.JSONArray
import org.json.JSONObject

/**
 * Class responsible for creating JSON object that will be
 * used to open Google Pay
 *
 * @param [price] final price of transaction
 * @param [merchantName] your store name
 * @param [merchantId] your merchant id in Tpay system
 */
class GooglePayRequest(
    price: Double,
    merchantName: String,
    merchantId: String
) : JSONObject() {
    init {
        put(API_VERSION_JSON, API_VERSION)
        put(API_VERSION_MINOR_JSON, API_VERSION_MINOR)
        put(MERCHANT_INFO, getMerchantInfo(merchantName))
        put(ALLOWED_PAYMENT_METHODS, getAllowedPaymentMethods(merchantId))
        put(TRANSACTION_INFO, getTransactionInfo(price))
    }

    companion object {
        private val ALLOWED_AUTH_METHODS = JSONArray(listOf("PAN_ONLY"))
        private val ALLOWED_CARD_NETWORKS = JSONArray(listOf("MASTERCARD", "VISA"))
        private const val GATEWAY = "tpaycom"
        private const val API_VERSION = 2
        private const val API_VERSION_JSON = "apiVersion"
        private const val API_VERSION_MINOR = 0
        private const val API_VERSION_MINOR_JSON = "apiVersionMinor"
        private const val MERCHANT_INFO = "merchantInfo"
        private const val ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods"
        private const val TRANSACTION_INFO = "transactionInfo"
        private const val TOTAL_PRICE_STATUS = "totalPriceStatus"
        private const val FINAL = "FINAL"
        private const val TOTAL_PRICE = "totalPrice"
        private const val CURRENCY_CODE = "currencyCode"
        private const val TYPE = "type"
        private const val PAYMENT_GATEWAY = "PAYMENT_GATEWAY"
        private const val PARAMETERS = "parameters"
        private const val GATEWAY_JSON = "gateway"
        private const val GATEWAY_MERCHANT_ID_JSON = "gatewayMerchantId"
        private const val MERCHANT_NAME = "merchantName"
        private const val CARD = "CARD"
        private const val ALLOWED_AUTH_METHODS_JSON = "allowedAuthMethods"
        private const val ALLOWED_CARD_NETWORKS_JSON = "allowedCardNetworks"
        private const val TOKENIZATION_SPECIFICATION = "tokenizationSpecification"
        private const val PLN = "PLN"

        private fun getTransactionInfo(
            price: Double,
            currencyCode: String = PLN
        ) = JSONObject().apply {
            put(TOTAL_PRICE_STATUS, FINAL)
            put(TOTAL_PRICE, price.toString())
            put(CURRENCY_CODE, currencyCode)
        }

        private fun getTokenizationSpecification(
            merchantId: String
        ) = JSONObject().apply {
            put(TYPE, PAYMENT_GATEWAY)
            put(
                PARAMETERS,
                JSONObject().apply {
                    put(GATEWAY_JSON, GATEWAY)
                    put(GATEWAY_MERCHANT_ID_JSON, merchantId)
                }
            )
        }

        private fun getMerchantInfo(merchantName: String) = JSONObject().apply {
            put(MERCHANT_NAME, merchantName)
        }

        private fun getAllowedPaymentMethods(merchantId: String) = JSONArray(
            listOf(
                JSONObject().apply {
                    put(TYPE, CARD)
                    put(
                        PARAMETERS,
                        JSONObject().apply {
                            put(ALLOWED_AUTH_METHODS_JSON, ALLOWED_AUTH_METHODS)
                            put(ALLOWED_CARD_NETWORKS_JSON, ALLOWED_CARD_NETWORKS)
                        }
                    )
                    put(TOKENIZATION_SPECIFICATION, getTokenizationSpecification(merchantId))
                }
            )
        )
    }
}