package com.tpay.sdk.api.screenless.googlePay

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import org.json.JSONObject

/**
 * Class providing utils for Google Pay
 *
 * @param [activity] activity used to receive onActivityResult
 * @param [googlePayRequest] information about price, your store name and your merchantId in Tpay system
 * @param [googlePayEnvironment] google pay environment that you want to use
 * @param [customRequestCode] alternative request code that will be used to manage Google Pay data
 */
class GooglePayUtil(
    private val activity: Activity,
    private val googlePayRequest: GooglePayRequest,
    googlePayEnvironment: GooglePayEnvironment = GooglePayEnvironment.PRODUCTION,
    private val customRequestCode: Int? = null
) {
    private val paymentsClient = Wallet.WalletOptions.Builder()
        .setEnvironment(googlePayEnvironment.actual)
        .build()
        .let { walletOptions -> Wallet.getPaymentsClient(activity, walletOptions) }

    /**
     * Method responsible for starting the Google Pay module
     */
    fun openGooglePay() {
        val paymentDataRequest = PaymentDataRequest.fromJson(googlePayRequest.toString())

        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            activity,
            customRequestCode ?: GOOGLE_PAY_SCREENLESS_REQUEST_CODE
        )
    }

    /**
     * Method responsible for handling activity result with
     * [customRequestCode] or [GOOGLE_PAY_SCREENLESS_REQUEST_CODE] code.
     * Use this method on the same object that method [openGooglePay] was called.
     *
     * @param [requestCode] request code from Activity.onActivityResult method
     * @param [resultCode] result code from Activity.onActivityResult method
     * @param [data] intent from Activity.onActivityResult method
     * @param [result] callback with the result of Google Pay module
     */
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        result: (OpenGooglePayResult) -> Unit
    ) {
        if (requestCode == (customRequestCode ?: GOOGLE_PAY_SCREENLESS_REQUEST_CODE)) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data
                        ?.let(PaymentData::getFromIntent)
                        ?.let(this::getGooglePayToken)
                        ?.let(result)
                        ?: result(OpenGooglePayResult.UnknownError)
                }
                Activity.RESULT_CANCELED -> result(OpenGooglePayResult.Cancelled)
                else -> result(OpenGooglePayResult.UnknownError)
            }
        }
    }

    private fun getGooglePayToken(paymentData: PaymentData): OpenGooglePayResult {
        return try {
            val data = JSONObject(paymentData.toJson())
            val paymentMethodData = data.getJSONObject(PAYMENT_METHOD_DATA)
            val info = paymentMethodData.getJSONObject(INFO)

            val token = paymentMethodData
                .getJSONObject(TOKENIZATION_DATA)
                .getString(TOKEN)

            val description = paymentMethodData.getString(DESCRIPTION)
            val cardNetwork = info.getString(CARD_NETWORK)
            val cardTail = info.getString(CARD_DETAILS)

            OpenGooglePayResult.Success(
                token = token,
                description = description,
                cardNetwork = cardNetwork,
                cardTail = cardTail
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            OpenGooglePayResult.UnknownError
        }
    }

    /**
     * Function responsible for checking if Google Pay is available
     *
     * @param [result] callback, if true Google Pay is available to use
     */
    fun checkIfGooglePayIsAvailable(result: (Boolean) -> Unit) {
        val isReadyToPayRequest = IsReadyToPayRequest.fromJson(googlePayRequest.toString())

        paymentsClient
            .isReadyToPay(isReadyToPayRequest)
            .apply {
                addOnCompleteListener { task ->
                    try {
                        task.getResult(ApiException::class.java).let(result)
                    } catch (exception: Exception) {
                        result(false)
                    }
                }
                addOnFailureListener { result(false) }
                addOnCanceledListener { result(false) }
            }
    }

    companion object {
        const val GOOGLE_PAY_SCREENLESS_REQUEST_CODE = 2002
        internal const val GOOGLE_PAY_UI_REQUEST_CODE = 2001
        private const val PAYMENT_METHOD_DATA = "paymentMethodData"
        private const val TOKENIZATION_DATA = "tokenizationData"
        private const val TOKEN = "token"
        private const val DESCRIPTION = "description"
        private const val CARD_NETWORK = "cardNetwork"
        private const val CARD_DETAILS = "cardDetails"
        private const val INFO = "info"
    }
}