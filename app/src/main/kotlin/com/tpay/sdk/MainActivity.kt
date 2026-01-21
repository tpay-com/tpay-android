package com.tpay.sdk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.tpay.sdk.api.models.*
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.models.transaction.SingleTransaction
import com.tpay.sdk.api.paycard.CreditCardBrand
import com.tpay.sdk.api.payment.Payment
import com.tpay.sdk.api.providers.MerchantDetailsProvider
import com.tpay.sdk.api.providers.SSLCertificatesProvider
import com.tpay.sdk.api.screenless.googlePay.GooglePayUtil
import com.tpay.sdk.api.tpayModule.TpayModule

class MainActivity : AppCompatActivity() {
    private val payer = Payer(
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "123456789",
        address = null
    )

    private lateinit var paymentSheet: Payment.Sheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(R.layout.activity_main)

        paymentSheet = Payment.Sheet(
            transaction = SingleTransaction(
                amount = 29.99,
                description = "transaction description",
                payerContext = PayerContext(
                    payer,
                    automaticPaymentMethods = AutomaticPaymentMethods(
                        blikAlias = BlikAlias.Registered(value = "<alias value>", label = "<alias label>"),
                        tokenizedCards = listOf(TokenizedCard("<card token>", "<card tail>", CreditCardBrand.MASTERCARD))
                    )
                ),
                notifications = null
            ),
            activity = this,
            supportFragmentManager = supportFragmentManager
        )

        TpayModule
            .configure(object : SSLCertificatesProvider {
                override var apiConfiguration: CertificatePinningConfiguration = CertificatePinningConfiguration(
                    publicKeyHash = "<public key hash>"
                )
            })
            .configure(Environment.SANDBOX)
            .configure(PaymentMethod.allMethods)
            .configure(Language.PL)
            .configure(GooglePayConfiguration("<merchant id>"))
            .configure(object : MerchantDetailsProvider {
                override fun merchantDisplayName(language: Language): String {
                    return when(language){
                        Language.PL -> "<polish name>"
                        Language.EN -> "<english name>"
                    }
                }

                override fun merchantCity(language: Language): String {
                    return when (language) {
                        Language.PL -> "<polish headquarters>"
                        Language.EN -> "<english headquarters>"
                    }
                }

                override fun regulationsLink(language: Language): String {
                    return when (language) {
                        Language.PL -> "<polish regulations url>"
                        Language.EN -> "<english regulations url>"
                    }
                }
            })
            .configure(
                Merchant(
                    authorization = Merchant.Authorization(
                        clientId = "<client id>",
                        clientSecret = "<client secret>"
                    )
                )
            )

        findViewById<PayButton>(R.id.payBtn).setOnClickListener {
            val result = paymentSheet.present()
            println(result.javaClass.simpleName)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val isPaymentSheetOpen = Payment.Sheet.isOpen(supportFragmentManager)
        val isGooglePayResult = requestCode == GooglePayUtil.GOOGLE_PAY_UI_REQUEST_CODE

        if (isPaymentSheetOpen && isGooglePayResult) {
            Payment.Sheet.onActivityResult(supportFragmentManager, requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        paymentSheet.onBackPressed()
    }
}