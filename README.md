# Tpay Android SDK
[![Min Android SDK](https://img.shields.io/badge/Min%20sdk-23-informational.svg)](https://shields.io/)[![Target Android SDK](https://img.shields.io/badge/Target/Compile%20sdk-33-informational.svg)](https://shields.io/)

## About
This SDK allows your app to make payments with Tpay.

## Install
Tpay SDK is available on Maven Central.
```groovy
// Add Maven Central repository to root level build.gradle or settings.gradle file
repositories {
    mavenCentral()
}

// Add Tpay SDK dependency to app level build.gradle
dependencies {
    implementation "com.tpay:sdk:<version>"   
}
```

# Usage
Tpay SDK contains UI module that users can interact with and exposes a possibility to make screenless payments.

## Configuration
To configure Tpay SDK use TpayModule class.

### Merchant
Configure information about merchant.
```kotlin
TpayModule.configure(  
    Merchant(
        merchantId = "YOUR_MERCHANT_ID",
        authorization = Merchant.Authorization(  
            clientId = "YOUR_CLIENT_ID",  
            clientSecret = "YOUR_CLIENT_SECRET"  
        )
    )  
)
```
### Environment
Configure Tpay environment
```kotlin
TpayModule.configure(Environment.PRODUCTION)
```

### Languages
Configure languages that customer will see when using Tpay UI module.

```kotlin
TpayModule.configure(  
    preferredLanguage = Language.PL,  
    supportedLanguages = listOf(Language.PL, Language.EN)  
)
```

### Payment methods
Configure payment methods that customer will be able to use.
```kotlin
TpayModule.configure(  
    paymentMethods = listOf(  
        PaymentMethod.Card,  
        PaymentMethod.Blik,  
        PaymentMethod.Pbl,  
        PaymentMethod.DigitalWallets(  
            wallets = listOf(DigitalWallet.GOOGLE_PAY)  
        )  
    )  
)
```

### Merchant details
Configure merchant details provider to make sure information is correctly displayed for selected language.
```kotlin
TpayModule.configure(object : MerchantDetailsProvider {  
    override fun merchantDisplayName(language: Language): String {  
        return when (language) {  
            Language.PL -> "polish name"  
            Language.EN -> "english name"  
        }  
    }

    override fun merchantCity(language: Language): String {
        return when (language) {
            Language.PL -> "Warszawie"
            Language.EN -> "Warsaw"
        }
    }
    
    override fun regulationsLink(language: Language): String {  
        return when (language) {  
            Language.PL -> "polish regulation url"  
            Language.EN -> "english regulation url"  
        }  
    }  
})
```

### Certificate configuration
Configure public key that will be used to encrypt credit card data.
```kotlin
TpayModule.configure(object : SSLCertificatesProvider {  
    override var apiConfiguration: CertificatePinningConfiguration =  
        CertificatePinningConfiguration(publicKeyHash = "PUBLIC_KEY")  
})
```

## Back press handling
Tpay UI sheets need to handle system back press events.
```kotlin
override fun onBackPressed() {  
    sheet.onBackPressed()  
}
```
## Activity result handling
Configure activity result handling when using Google Pay.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    // Where paymentSheet is a object of Payment.Sheet class
    paymentSheet.onActivityResult(requestCode, resultCode, data)
}
```
## Payment
Payment flow opens a UI module and allows customer to pick one of defined payment methods.
```kotlin
// Create payment
val paymentSheet = Payment.Sheet(  
    object : Transaction {  
        override val amount: Double = 29.99  
        override val description: String = "transaction description"  
        override val payerContext: PayerContext = PayerContext(  
            payer = Payer(  
                name = "Jan Kowalski",  
                email = "jan.kowalski@example.com",  
                phone = null,  
                address = null  
            ),
            automaticPaymentMethods = AutomaticPaymentMethods(
                blikAlias = BlikAlias.Registered(value = "alias value", label = "alias label"),
                tokenizedCards = listOf(
                    TokenizedCard(token = "card token 1", cardTail = "1234", brand = CreditCardBrand.VISA),
                    TokenizedCard(token = "card token 2", cardTail = "1234", brand = CreditCardBrand.MASTERCARD)
                )
            )
        )  
        override val notifications: Notifications = Notifications(  
            notificationEmail = "payments@yourstore.com",  
            notificationUrl = "https://yourstore.com"  
        )  
    },
    activity = activity,
    supportFragmentManager = activity.supportFragmentManager
)

// Add payment observer
paymentSheet.addObserver(object : PaymentDelegate {  
    override fun onPaymentCreated(transactionId: String?) { }  
    override fun onPaymentCompleted(transactionId: String?) { }  
    override fun onPaymentCancelled(transactionId: String?) { }
    override fun onModuleClosed() { }  
})

// Open UI module
val result = paymentSheet.present()
if (result is SheetOpenResult.Success) {
    // Module opened successfully
}
```
## Token payment
Token payment allows for making credit card token payments with Tpay UI.
```kotlin
// Create payment
val tokenPaymentSheet = CardTokenPayment.Sheet(  
    transaction = CardTokenTransaction(
        amount = 29.99,
        description = "transaction description",
        payer = Payer(
            name = "Jan Kowalski",
            email = "jan.kowalski@example.com",
            phone = null,
            address = null
        ),
        cardToken = "card token",
        notifications = "https://yourstore.com"
    ),
    activity = activity,
    supportFragmentManager = activity.supportFragmentManager
)

// Add payment observer
tokenPaymentSheet.addObserver(object : PaymentDelegate {  
    override fun onPaymentCreated(transactionId: String?) { }
    override fun onPaymentCompleted(transactionId: String?) { }
    override fun onPaymentCancelled(transactionId: String?) { }
    override fun onModuleClosed() { }
})

// Open UI module
val result = tokenPaymentSheet.present()
if (result is SheetOpenResult.Success) { 
    // Module opened successfully
}
```
## Tokenization
Tpay SDK allows customers to tokenize credit cards to save them for future payments.
```kotlin
// Create tokenization
val tokenizationSheet = AddCard.Sheet(  
    tokenization = Tokenization(  
        payer = Payer(  
            name = "Jan Kowalski",  
            email = "jan.kowalski@example.com",  
            phone = null,  
            address = null  
        ),  
        notificationUrl = "https://yourstore.com"  
    ),
    activity = activity,
    supportFragmentManager = activity.supportFragmentManager
)

// Add tokenization observer
tokenizationSheet.addObserver(object : AddCardDelegate {  
    override fun onAddCardSuccess(tokenizationId: String?) { }
    override fun onAddCardFailure() { }
    override fun onModuleClosed() { }  
})

// Open UI module
val result = tokenizationSheet.present()
if (result is SheetOpenResult.Success) { 
    // Module opened successfully
}
```
# Screenless payments
## Get payment methods
GetPaymentMethods class allows you to get payment methods that you can use within your app. It takes a common part of payment methods available on Tpay backend and payment methods that you configured using TpayModule.
```kotlin
GetPaymentMethods().execute { result ->  
    if (result is GetPaymentMethodsResult.Success) {  
        if (result.isCreditCardPaymentAvailable) {  
            // show credit card  
        }  
        if (result.isBLIKPaymentAvailable) {  
            // show BLIK  
        }  
        if (result.availableTransferMethods.isNotEmpty()) {
            // show transfers
            // each transfer object contains         
            // groupId, name and image url
        }
        if (result.availableDigitalWallets.isNotEmpty()) {  
            // show digital wallets  
        }  
    }  
}
```
## Parameters
All screenless payments require payment details and payer information, you can also pass redirects and notifications.
```kotlin
val payer = Payer(  
    name = "Jan Kowalski",  
    email = "jan.kowalski@example.com",  
    phone = null,  
    address = null  
)

val paymentDetails = PaymentDetails(  
    amount = 29.99,  
    description = "transaction description",  
    hiddenDescription = "hidden description",  
    language = Language.PL  
)

val redirects = Redirects(  
    successUrl = "https://yourstore.com/success",  
    errorUrl = "https://yourstore.com/error"  
)  
  
val notifications = Notifications(  
    notificationEmail = "payments@yourstore.com",  
    notificationUrl = "https://yourstore.com"  
)
```
## Screenless credit card payment
CreditCardPayment allows you to create payments with credit card data or credit card token (for returning customers) and can also create recursive payments.
```kotlin
val calendar = Calendar.getInstance()  
calendar.set(2024, 6, 1)  
  
CreditCardPayment.Builder()  
    .setRecursive(  
        Recursive(  
            frequency = Frequency.MONTHLY,  
            quantity = Quantity.Specified(3),  
            expirationDate = calendar.time  
        )  
    )  
    .setCallbacks(redirects, notifications)  
    .setPayer(payer)  
    .setCreditCard(  
        CreditCard("1111111111111111", "02/29", "123"),  
        domain = "https://yourstore.com"  
    )  
    .setCreditCardToken("card token")
    .setPaymentDetails(paymentDetails)  
    .build()
    .execute { result ->
        // handle payment create result
    }
```
## Screenless BLIK payment
BLIKPayment allows you to create payments with BLIK code or alias (for returning customers).
```kotlin
BLIKPayment.Builder()  
    .setBLIKCode(code = "123456")
    .setBLIKCodeAndRegisterAlias(
        code = "123456", 
        blikAlias = BlikAlias.NotRegistered(value = "value", label = "label")
    )
    .setBLIKAlias(BlikAlias.Registered(value = "value", label = "label"))
    .setPayer(payer)  
    .setPaymentDetails(paymentDetails)
    .setCallbacks(redirects, notifications)
    .build()  
    .execute { result ->  
        // handle payment create result
    }
```
BLIKPayment can return a result of type CreateBLIKTransactionResult.AmbiguousBlikAlias. In this case you need to display result.aliases to the user and then use BLIKAmbiguousAliasPayment class to continue the payment.
```kotlin
if (result is CreateBLIKTransactionResult.AmbiguousBlikAlias) {
    // display result.aliases
    // continue with BLIKAmbiguousAliasPayment if user selected a alias

    BLIKAmbiguousAliasPayment
        .from(
            transactionId = result.transactionId, // id of transaction created with BLIKPayment
            blikAlias = BlikAlias.Registered(value = "value", label = "label"), // blik alias used to create payment with BLIKPayment
            ambiguousAlias = AmbiguousAlias(...) // ambiguous alias selected by user
        )
        .execute { result ->
            // handle result
        }   
}
```
## Screenless transfer payment
TransferPayment allows you to create payment with bank selected by user identified by groupId.
```kotlin
TransferPayment.Builder()  
    .setGroupId(102)  
    .setPayer(payer)  
    .setPaymentDetails(paymentDetails)  
    .setCallbacks(redirects, notifications)  
    .build()  
    .execute { result -> 
        // handle payment create result
    }
```
## Screenless Google Pay payment
GooglePayPayment allows you to create payments with credit card data provided by Google Pay.
```kotlin
GooglePayPayment.Builder()
    .setGooglePayToken("GOOGLE_PAY_TOKEN")
    .setPayer(payer)
    .setPaymentDetails(paymentDetails)
    .setCallbacks(redirects, notifications)
    .build()
    .execute { result ->
        // handle payment create result
    }
```
To make things easier you can use GooglePayUtil class.
```kotlin
val googlePayUtil = GooglePayUtil(
    activity = requireActivity(),
    googlePayRequest = GooglePayRequest(
        price = 19.99, // Final price
        merchantName = "Your Store",
        merchantId = "YOUR_MERCHANT_ID" // Tpay merchant id
    ),
    googlePayEnvironment = GooglePayEnvironment.PRODUCTION
)

// Check if Google Pay is available to use on the device
googlePayUtil.checkIfGooglePayIsAvailable { isAvailable ->
    if (isAvailable) {
        // show Google Pay button
    }
}

// Opens the Google Pay module with data specified in GooglePayRequest
googlePayUtil.openGooglePay()

// Google Pay requires your app to override onActivityResult method in Activity
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayUtil.handleActivityResult(requestCode, resultCode, data) { result ->
        when (result) {
            is OpenGooglePayResult.Success -> {
                // Payer selected credit card
                // use result.token with GooglePayPayment.Builder
                // to create payment
            }
            is OpenGooglePayResult.Cancelled -> {
                // Google Pay request was cancelled
            }
            is OpenGooglePayResult.UnknownError -> {
                // Unknown error occurred
            }
        }
    }
}
```
## Handling result
Screenless payments can be instantly paid when creating payment (it can happen with credit card payment) but most likely the payment will be created and you will have to display payment url received via result to the user. BLIK payment doesn't receive payment url because user has to accept payment in bank app so it's recommended to use long polling mechanism in that case.
## Long polling
Screenless payments give you a option to use long polling mechanism. You have to pass LongPollingConfig object as a first parameter of execute function as shown on BLIKPayment.
Note that long polling mechanism will start only when it's needed:
- CreditCardPayment: when payment is created and you have to display payment url.
- BLIKPayment or BLIKAmbiguousAliasPayment: when payment is created and payer has to accept payment in bank app.
- TransferPayment: when payment is created and you have to display payment url.
- GooglePayPayment: when payment is created and you have to display payment url.
```kotlin
val config = LongPollingConfig(  
    delayMillis = 4000, // delay between requests
    maxRequestCount = 10, 
    stopOnFirstRequestError = false, 
    onTransactionState = { 
        // this function is called on each response 
        // with new transaction state 
        // long polling keeps running if transaction 
        // state is PENDING
    },  
    onRequestError = { 
        // request error  
    },  
    onMaxRequestCount = { 
        // maximum request count was reached 
        // long polling stops and you can show timeout to user 
        // this means that we don't know if payment was successful
    }  
)

BLIKPayment.Builder()  
    .setBLIKCode("123456")
    .setPayer(payer)  
    .setPaymentDetails(paymentDetails)
    .setCallbacks(redirects, notifications)
    .build()  
    .execute(config) { result ->  
        // handle payment create result
    }
```

## License
This library is released under the [MIT License](https://opensource.org/license/mit/).