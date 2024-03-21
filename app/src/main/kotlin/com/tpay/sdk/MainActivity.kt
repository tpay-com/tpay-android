package com.tpay.sdk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tpay.sdk.api.models.*
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.models.payer.Payer
import com.tpay.sdk.api.models.transaction.Transaction
import com.tpay.sdk.api.paycard.CreditCardBrand
import com.tpay.sdk.api.payment.Payment
import com.tpay.sdk.api.providers.MerchantDetailsProvider
import com.tpay.sdk.api.providers.SSLCertificatesProvider
import com.tpay.sdk.api.screenless.Notifications
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
        setContentView(R.layout.activity_main)

        paymentSheet = Payment.Sheet(
            transaction = object : Transaction {
                override val amount: Double
                    get() = 29.99
                override val description: String
                    get() = "transaction description"
                override val payerContext: PayerContext
                    get() = PayerContext(
                        payer,
                        automaticPaymentMethods = AutomaticPaymentMethods(
                            blikAlias = BlikAlias.Registered(value = "<alias value>", label = "<alias label>"),
                            tokenizedCards = listOf(TokenizedCard("<card token>", "<card tail>", CreditCardBrand.MASTERCARD))
                        )
                    )
                override val notifications: Notifications? = null
            },
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
        paymentSheet.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        paymentSheet.onBackPressed()
    }
}