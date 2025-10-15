# Tpay Android SDK

![Static Badge](https://img.shields.io/badge/min_android_sdk-23-blue?logo=android&label=Min%20Android%20SDK)
[![Target Android SDK](https://img.shields.io/badge/Target/Compile%20sdk-34-informational.svg)](https://shields.io/) 
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.0-informational.svg?logo=kotlin)](https://shields.io/)
[![Java](https://img.shields.io/badge/Java-8-informational.svg?logo=java)](https://shields.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## About

This SDK allows your app to make payments with Tpay.
Package documentation is available [here](https://tpay-com.github.io/tpay-android/).

Minimum supported versions:

| Library | Version |
| ------- | ------- |
| Android SDK | 23 (Android 6.0, Marshmallow) |
| Compile/Target SDK | 34 (Android 14, Upside Down Cake) |
| Kotlin | 1.8.0 |
| Java | 8|

> [!warning]
> For this SDK to work you will need `client_id` and `client_secret` tokens. You can find in [merchant's panel](https://panel.tpay.com/?lang=1).
>
> If you are partner, you can obtain them in your merchant partner account. For detailed
> instructions how to do that or how to create such an account
> check [this site](https://docs-api.tpay.com/en/merchant-accounts/).

> [!tip]
> To be able to test the SDK properly,
> use [mock data](https://support.tpay.com/sprzedawca/srodowisko-testowe-sandbox).

## Install

### Maven

Add Maven Central repository to root level build.gradle or settings.gradle file

```kotlin
repositories {
    mavenCentral()
}
```

Add Tpay SDK dependency to libs.versions.toml file

```toml
tpay = "<newest_version>"

[libraries]
tpay = { module = "com.tpay:sdk", version.ref = "tpay" }
```

Add Tpay SDK dependency to app level build.gradle

```kotlin
dependencies {
    // Tpay SDK
    implementation(libs.tpay)
}
```

**OR**

simply add implementation to the app level build.gradle file

```kotlin
dependencies {
    // Tpay SDK
    implementation("com.tpay:sdk:<newest_version>")
}
```

### Local

Tpay SDK is also available as a local maven repository, downloadable from GitHub releases.

1. Unzip the downloaded file
2. Add a local maven repository to root level build.gradle or settings.gradle file

```kotlin
repositories {
    maven {
        url "/path/to/tpayMaven"
    }
}
```

3. Add Tpay SDK dependency to app level build.gradle

```kotlin
// Local repository contains only one SDK version
dependencies {
    implementation "com.tpay:sdk:<downloaded_version>"
}
```

### Proguard/R8

If you are using Proguard/R8 in your project, you have to add the following rules to the
`app/proguard-rules.pro` file, to keep Tpay SDK classes

```proguard
# Keep all Tpay sdk classes
-keep class com.tpay.sdk.** { *; }
```

## Configuration

> [!note]
> In this section we will provide examples for each configuration to the TpayModule class
> you will be able to make.

> [!important]
> Beneath you will find all configurations that are **MANDATORY**.

### Initialization

At first, you have to configure your app to be able to make any requests by providing SDK info about
your merchant account.
Info about `client_id` and `client_secret` you will find in your merchant's panel at `Integration ->
API`.

```kotlin
TpayModule.configure(
    Merchant(
        authorization = Merchant.Authorization(
            clientId = "client_id",
            clientSecret = "client_secret",
        )
    )
)
```

### Environment

Tpay SDK provides two types of environments you can use in your app:

* `Environment.SANDBOX` - used only for tests and in stage/dev flavor.
* `Environment.PRODUCTION` - used for production flavors.

```kotlin
TpayModule.configure(Environment.SANDBOX)
```

### Payment methods

For users to be able to use a specific payment method you have declared it in the configuration.

| Method | Description                                                              |
| ------ |--------------------------------------------------------------------------|
| BLIK | [Web docs](https://docs-api.tpay.com/en/payment-methods/blik/)           |
| Pbl **(Pay-By-Link)** | [Web docs](https://docs-api.tpay.com/en/payment-methods/pbl/)            |
| Card | [Web docs](https://docs-api.tpay.com/en/payment-methods/cards/)          |
| DigitalWallets | [GOOGLE_PAY](https://docs-api.tpay.com/en/payment-methods/google-pay/)   |
| InstallmentPayments | [RATY_PEKAO](https://docs-api.tpay.com/en/payment-methods/installments/) |
| DeferredPayments **(BNPL)** | [PAY_PO](https://docs-api.tpay.com/en/payment-methods/bnpl/)             |

> [!note]
> As default, if no method were provided, all methods are being set up.

<br>

```kotlin
TpayModule.configure(
    paymentMethods = listOf(
        PaymentMethod.Card,
        PaymentMethod.Blik,
        PaymentMethod.Pbl,
        PaymentMethod.DigitalWallets(
            wallets = listOf(DigitalWallet.GOOGLE_PAY)
        ),
        PaymentMethod.InstallmentPayments(
            methods = listOf(
                InstallmentPayment.RATY_PEKAO,
                InstallmentPayment.PAY_PO,
            ),
        ),
    ),
)
```

#### Card

If you decide to enable the credit card payment option, you have to provide SSL certificates.

> [!tip]
> You can find public key on you merchant panel:
> - Acquirer Elavon: `Credit card payments -> API`
> - Acquirer Pekao: `Integrations -> API -> Cards API`

```kotlin
TpayModule.configure(
    object : SSLCertificatesProvider {
        override var apiConfiguration: CertificatePinningConfiguration =
            CertificatePinningConfiguration(
                publicKeyHash = "public_key",
            )
    },
)
```

#### Google Pay configuration

In order to be able to use Google Pay method you have to provide your `merchant_id` to the SDK.

> [!tip]
> Your login name to the merchant panel is your merchant id.

```kotlin
TpayModule.configure(GooglePayConfiguration("merchant_id"))
```

<br>

> [!important]
> Beneath you will find all configurations that are **OPTIONAL**.

### Languages

Tpay SDK lets you decide what languages will be available in the Tpay's screen and which one of them
will be preferred/default.

Right now, SDK allows you to use 2 languages:

* `Language.PL` - polish
* `Language.EN` - english

> [!warning]
> If you do not choose to configure languages at all, by default, all available languages will be
> supported and polish will be preferred one.

```kotlin
TpayModule.configure(
    preferredLanguage = Language.PL,
    supportedLanguages = listOf(Language.PL, Language.EN),
)
```

### Merchant details

As a merchant, you can configure how information about you will be shown.
You can set up your `display name`, `city` and `regulations link`.
You can choose to provide different copy for each language or simply use one for all.

```kotlin
TpayModule.configure(
    object : MerchantDetailsProvider {
        override fun merchantDisplayName(language: Language): String {
            return when (language) {
                Language.PL -> "polish name"
                Language.EN -> "english name"
            }
        }

        override fun merchantCity(language: Language): String {
            return when (language) {
                Language.PL -> "Warszawa"
                Language.EN -> "Warsaw"
            }
        }

        override fun regulationsLink(language: Language): String = "regulations link"
    },
)
```

### Compatibility

Tpay SDK allows you to choose with which platform it should be compatible.
Currently, you can choose one of the 3 options:

* `Compatibility.NATIVE` - used for native android development.
* `Compatibility.FLUTTER` - used if you want to create your own module for Tpay that will be used in
  flutter app. We also have [official Flutter SDK](https://github.com/tpay-com/tpay-flutter) as
  well.
* `Compatibility.REACT_NATIVE` - used if you want to create your own module for Tpay that will be
  used in react native app. We also
  have [official React Native SDK](https://github.com/tpay-com/tpay-react-native) as well.

> [!note]
> As the default one, the `Compatibility.NATIVE` will be used.

```kotlin
// For native development
TpayModule.configure(Compatibility.NATIVE)

// For Flutter plugin
TpayModule.configure(Compatibility.FLUTTER)

// For React Native module
TpayModule.configure(Compatibility.REACT_NATIVE)
```

## Handling payments

Tpay SDK provides two ways of handling payments:

- `Official SDK screens` - you can use Tpay's official screens where you just need to provide "soft"
  information, like price, description or payer info.
- `Screenless` - you can use screenless functionalities, where you set callbacks for payments and
  display all necessary information on your own screens.

## Official SDK screens

> [!warning]
> Screens made by the Tpay team are based on view system, so under the hood they rely on activity
> and fragment managers!

To make integration with the SDK faster, we created 4 types of sheets that can be used to handle
payments:

* `Payment` - the most simple screen where the user can choose any payment method and proceed with it.
* `AddCard` - screen that handles generating payment token from the credit card.
* `CardTokenPayment` - screen that handles payment with a previously created token for a credit card.
* `Webview` - screens that handle payment through webview with a specific link generated via API.

> [!important]
> No matter which method you choose, to display it to user, simply call `present()` function on the
> presentable screen object.

### Payment

Payment flow opens a UI module and allows the customer to pick one of defined the payment methods.
This method requires setting up a few things in order to fulfill payment:

* `amount` - simply the price of the transaction
* `description` - transaction description
* `hiddenDescription` (optional) - description visible only to the merchant
* `payerContext` - information about payer
    * `payer` - information about the person who is making the payment
        * `name` - payer name
        * `email` - payer email
        * `phone` - payer phone number
        * `address` - payer address
            * `city` - city name
            * `countryCode` - country code in ISO 3166-1 alpha-2 format
            * `address` - street address
            * `postalCode` - postal code
    * `automaticPaymentMethods` - configuration of automatic payments
        * `tokenizedCards` - previously saved credit cards
            * `token` - card token
            * `cardTails` - last 4 digits of the card
            * `brand` - card brand
        * `blikAlias` - previously saved BLIK alias
            * `value` - alias value
            * `label` - alias label
* `notifications` - info about where the merchant should be notified about new transactions
    * `notificationEmail` - email address to send notification to
    * `notificationUrl` - URL to send notification to / URL to send tokens for tokenization
* `activity` - activity to associate the view with
* `supportFragmentManager` - fragment manager to associate the view with

```kotlin
val paymentSheet = Payment.Sheet(
    transaction = SingleTransaction(
        amount = 29.99,
        description = "transaction description",
        payerContext = PayerContext(
            payer = Payer(
                name = "John Doe",
                email = "john.doe@test.pl",
                phone = "123456789",
                address = Payer.Address(
                    city = "Warsaw",
                    countryCode = "PL",
                    address = "Test Street 1",
                    postalCode = "00-001"
                ),
            ),
        ),
        notifications = Notifications(
            notificationEmail = "payments@yourstore.com",
            notificationUrl = "https://yourstore.com"
        )
    ),
    activity = activity,
    supportFragmentManager = activity.supportFragmentManager,
)
```

Payment method also provides a way of observing the status of the payment in real time.

```kotlin
paymentSheet.addObserver(
    object : PaymentDelegate {
        override fun onPaymentCreated(transactionId: String?) {}
        override fun onPaymentCompleted(transactionId: String?) {}
        override fun onPaymentCancelled(transactionId: String?) {}
        override fun onModuleClosed() {}
    }
)
```

> [!important]
> Tpay SDK also supports `NFC` and `camera` card scanning:
> * `NFC` - Adding card info during transaction, user can tap on the NFC button.
    Then, if NFC is enabled in the device, after holding the physical card near the device, SDK will scan
    the card's data and automatically fill the form with it.
> * `Camera` - Adding card info during transaction, user can tap on the camera button.
    Then, if the camera scans card data successfully, form will be filled automatically.

#### Activity result handling

If you enabled Google Pay payment method, you also have to enable payment presentable to handle
activity results.

```kotlin
 override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    caller: ComponentCaller,
) {
    super.onActivityResult(requestCode, resultCode, data, caller)

    // Where paymentSheet is an object of Payment.Sheet class
    paymentSheet.onActivityResult(requestCode, resultCode, data)
}
```

#### Automatic Payments

Using `Payment.Sheet` screen you can set up automatic BLIK or card payments.
Thanks to that, user will not have to enter BLIK/card data all over again each time making the
payment.

##### Automatic Card Payments

If a user using a card as a payment method will opt-in saving card, on successful payment, on the link
specified as `notificationUrl` Tpay backend will send information about saved card token, tail and
brand.
Next, your backend has to send it to you, so you can use this info next time the same user wants
to pay with the card.
When you already have all required information, you can add `automaticPaymentMethods` to the
`payerContext`.

```kotlin
automaticPaymentMethods = AutomaticPaymentMethods(
    tokenizedCards = listOf(
        TokenizedCard(
            token = "card_token_visa",
            cardTail = "1234",
            brand = CreditCardBrand.VISA,
        ),
        TokenizedCard(
            token = "card_token_mastercard",
            cardTail = "4321",
            brand = CreditCardBrand.MASTERCARD,
        )
    )
)
```

##### Automatic BLIK Payments

If a user using BLIK as a payment method will opt-in saving BLIK alias, next time the same user will
want to pay with BLIK, you can simply use a previously saved alias to make the payment even faster.
When you already have all the required information, you can add `automaticPaymentMethods` to the
`payerContext`.

```kotlin
automaticPaymentMethods = AutomaticPaymentMethods(
    blikAlias = BlikAlias.Registered(
        value = "1234",
        label = "alias_1234",
    ),
)
```

## Tokenization

Tpay SDK allows you to make credit card transactions without need of entering card's data each time.
Instead, you can create and use a token, associated with a specific card and user.

> [!important]
> There are 2 types of tokens you can use in transactions:
> * [Simple tokens](https://docs-api.tpay.com/en/tokenization/#tokenization-without-charging) -
    tokens that go with card data upon transaction.
> * [Network tokens](https://docs-api.tpay.com/en/tokenization/#tokenization-plus) -
    tokens that can be used without exposing the card details. Also, this token persists even if
    card expires and the user requests a new one.

> [!warning]
> For recurring payments, you can simply use created token to make transaction without need of user
> interaction.

### Creating card token

> [!warning]
> `notificationUrl` should be the URL handled by your backend, because there will be sent token from
> the successful token creation.

```kotlin
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
```

Add card screen method also provides a way of observing the status of the payment in real time.

```kotlin
tokenizationSheet.addObserver(
    object : AddCardDelegate {
        override fun onAddCardFailure() {}
        override fun onAddCardSuccess(tokenizationId: String?) {}
        override fun onModuleClosed() {}
    },
)
```

### Token payment

If you already have card token payment, you can simply proceed with an actual tokenization
transaction.

> [!warning]
> `cardToken` is a token sent to your backend during card tokenization process.

```kotlin
val tokenPaymentSheet = CardTokenPayment.Sheet(
    transaction = CardTokenTransaction(
        amount = 5.21,
        description = "Test transaction",
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001",
            ),
        ),
        notifications = Notifications(
            notificationEmail = "john.doe@test.pl",
            notificationUrl = "https://yourcompany.url",
        ),
        cardToken = "card_token",
    ),
    activity = this,
    supportFragmentManager = supportFragmentManager,
)
```

Token payment method also provides a way of observing the status of the payment in real time.

```kotlin
tokenPaymentSheet.addObserver(
    object : PaymentDelegate {
        override fun onModuleClosed() {}
        override fun onPaymentCancelled(transactionId: String?) {}
        override fun onPaymentCompleted(transactionId: String?) {}
        override fun onPaymentCreated(transactionId: String?) {}
    },
)
```

## Web view

Tpay SDK provides you also way of handling transactions via WebView. During configuration, you have to
provide 3 URL links:

1) `paymentUrl` - for the user to make an actual payment.
2) `successUrl` - for redirecting the user to the success page.
3) `errorUrl` - for redirecting the user to the failure page.

> [!warning]
> `paymentUrl` - you or your backend can generate transactionUrl using specific
> [Tpay API endpoint](https://docs-api.tpay.com/en/first-steps/first-transaction/#create-a-transaction).

```kotlin
val webViewSheet = WebView.Sheet(
    webViewConfiguration = WebViewConfiguration(
        paymentUrl = "payment_url",
        successUrl = "success_url",
        errorUrl = "error_url",
    ),
    activity = activity,
    supportFragmentManager = activity.supportFragmentManager,
)
```

Webview payment method also provides setting up a callback for checking for payment status in real
time.

```kotlin
webViewSheet.setCallback(
    object : WebViewCallback {
        override fun onPaymentFailure() {}
        override fun onPaymentSuccess() {}
        override fun onWebViewClosed() {}
    },
)
```

## Common

Each presentable payment method has specific functions that you can use with either one of them.

### Back press handling

To properly handle the sheet's back navigation, you have to pass presentable's `onBackPressed` action to
your system's one.

```kotlin
override fun onBackPressed() {
    sheet.onBackPressed()
}
```

### Handling process death

If the Tpay module was open during process death, the system will automatically open it again after the user
comes back to the app.
In this case you can check if the Tpay sheets are open:

```kotlin
Payment.Sheet.isOpen(/* fragment manager */)
CardTokenPayment.Sheet.isOpen(/* fragment manager */)
AddCard.Sheet.isOpen(/* fragment manager */)
WebView.Sheet.isOpen(/* fragment manager */)
```

If a sheet is open, you can use a 'restore' method to restore a callback and receive
transaction/tokenization information.

```kotlin
Payment.Sheet.restore(/* fragment manager */, /* respective callback */)
CardTokenPayment.Sheet.restore(/* fragment manager */, /* respective callback */)
AddCard.Sheet.restore(/* fragment manager */, /* respective callback */)
WebView.Sheet.restore(/* fragment manager */, /* respective callback */)
```

Tpay module also needs a way to receive activity result data after process death.
This step is important if you use Google Pay as a payment method.

> [!warning]
> The `Payment.Sheet.onActivityResult` differs from `paymentSheet.onActivityResult` in the way SDK
> handles the payment. This example is specifically used for after death process restoration. We do
> recommend using both `onActivityResult` methods to fully support Google Pay transaction process.

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // Check if Payment.Sheet is currently open and visible for the user
    val isPaymentSheetOpen = Payment.Sheet.isOpen(supportFragmentManager)

    // Check if the incoming result was initiated by Tpay module for Google Pay
    val isGooglePayResult = requestCode == GooglePayUtil.GOOGLE_PAY_UI_REQUEST_CODE

    if (isPaymentSheetOpen && isGooglePayResult) {
        Payment.Sheet.onActivityResult(supportFragmentManager, requestCode, resultCode, data)
    }
}
```

## Screenless Payments

Screenless payments are a special type of payment functionality that gives you the whole power of
payment process, but do not limit you to using predefined Tpay screens.

### Get payment channels

To be able to use screenless functionalities you will need to know which payment methods are
available to your merchant account. To get them, you can simply call `GetPaymentChannels().execute`
and set up result observer for them.

To make returned payments more readable we can use `GroupedPaymentChannels` to structure them in
specific types.

Last but not least, we have to filter them by the specific methods we have enabled in our app and by
the amount of the transaction using `AvailablePaymentMethods` class.

> [!important]
> Available methods are being filtered for the specific transaction amount, so you should use this
> functionality each time you want to start a payment process.

```kotlin
GetPaymentChannels().execute { result ->
    when (result) {
        is GetPaymentChannelsResult.Success -> {
            // Group payment channels by type
            val grouped = GroupedPaymentChannels.from(result.channels)

            // Get only available methods that satisfy payment constraints.
            // It returns a common part of "grouped" and provided "methods".
            // Amount needs to be a final price that will be used while creating transaction.
            val availableMethods = AvailablePaymentMethods.from(
                grouped = grouped,
                methods = listOf(
                    PaymentMethod.Blik,
                    PaymentMethod.Pbl,
                    PaymentMethod.Card,
                    PaymentMethod.DigitalWallets(listOf(DigitalWallet.GOOGLE_PAY)),
                    PaymentMethod.InstallmentPayments(
                        listOf(
                            InstallmentPayment.PAY_PO,
                            InstallmentPayment.RATY_PEKAO,
                        ),
                    ),
                ),
                amount = 39.99
            )
        }
        is GetPaymentChannelsResult.Error -> {
            // handle retrieving payment methods error
        }
    }
}
```

### Configuration

Before you run any screenless payment we do recommend setting up long polling configuration.
You will need this to be able to monitor each payment status, i.e. it's status, like `PENDING`,
`DECLINED` or `SUCCESS` in real time.

> [!warning]
> Note that long polling mechanism will start only when it's needed:
> - `CreditCardPayment`: when payment is created and you have to display payment URL.
> - `BLIKPayment` or `BLIKAmbiguousAliasPayment`: when payment is created and payer has to accept
    payment in bank app.
> - `TransferPayment`: when payment is created and you have to display payment URL.
> - `GooglePayPayment`: when payment is created and you have to display payment URL.

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
        // this means that we don't know if payment was successful or not
    }
)
```

> [!tip]
> In `onTransactionState` you can check if status you got where success by checking if it was listed
> in `TransactionState.SUCCESS_STATES`

To associate long polling config with your payment pass it to the `execute(config)` function when
starting transaction process.

### Screenless Credit Card Payment

CreditCardPayment allows you to create payments with credit card data

```kotlin
CreditCardPayment.Builder()
    .setCallbacks(
        redirects = Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@youdomain.com",
            notificationUrl = "https://yourstore.com",
        ),
    )
    .setPayer(payer = payer)
    .setCreditCard(
        creditCard = CreditCard(
            cardNumber = "4056 2178 4359 7258",
            expirationDate = "12/35",
            cvv = "123",
        ),
        domain = "https://yourstore.com",
        saveCard = true,
    )
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL,
        ),
    )
    .build()
    .execute(config) {
        // handling transaction result status
    }
```

> [!warning]
> If CreditCardPayment returns `Created` result, you have to handle `paymentUrl`
> sent with it and redirect user to it in order to complete the payment.

> [!warning]
> If CreditCardPayment returns `CreatedAndPaid` result,
> it means that result has been paid automatically during creation.

#### Tokenization

You can also Opt-in to generate a credit card token for future payments
if you want to let users pay for transactions with previously used card.
To do so, in `setCreditCard` method, set the `saveCard` to true.

```kotlin
CreditCardPayment.Builder()
    .setCallbacks(
        redirects = Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@youdomain.com",
            notificationUrl = "https://yourstore.com",
        ),
    )
    .setCreditCard(
        creditCard = CreditCard(
            cardNumber = "4056 2178 4359 7258",
            expirationDate = "12/35",
            cvv = "123",
        ),
        domain = "https://yourstore.com",
        saveCard = true,
    )
```

> [!warning]
> Generated card token will be sent to `notificationUrl` specified in the notifications callbacks.

If you already have a credit card token, you can then set up token payment omitting the credit card info.
To do so, use `setCreditCardToken` instead of `setCreditCard` method.

```kotlin
CreditCardPayment.Builder()
    .setCreditCardToken("credit_card_token")
    .setCallbacks(
        redirects = Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@youdomain.com",
            notificationUrl = "https://yourstore.com",
        ),
    )
    .setPayer(payer = payer)
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL,
        ),
    )
    .build()
    .execute(config) {
        // handling callbacks
    }
```

#### Recurring Payments

`CreditCardPayment` let's you set up the recurring payments as well, so you don't have to remember
to charge your customer for your service periodically.

> [!important]
> You can choose one of the specified recurring payment frequencies:
> `DAILY`, `WEEKLY`, `MONTHLY`,`QUARTERLY` or `YEARLY`.

> [!important]
> You can choose to either charge user specified times using `Quantity.Specified` option,
> or to set it up to being charged until expiration date is being hit or user cancels subscription
> on his own with `Quantity.Indefinite`.

```kotlin
val expirationDate = Calendar.getInstance().apply {
    set(Calendar.YEAR, 2030)
    set(Calendar.MONTH, Calendar.DECEMBER)
    set(Calendar.DAY_OF_MONTH, 10)
}.time

CreditCardPayment.Builder()
    .setRecursive(
        recursive = Recursive(
            frequency = Frequency.MONTHLY,
            quantity = Quantity.Indefinite,
            expirationDate = expirationDate,
        )
    )
    .setCreditCardToken("card_token")
// rest of credit card payment configuration
```

### Screenless BLIK Payment

Tpay SDK let's make transactions with BLIK as well. Simply use `BLIKPayment` class.

```kotlin
BLIKPayment.Builder()
    .setBLIKCode(code = "777462")
    .setPayer(
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001"
            ),
        ),
    )
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL
        ),
    )
    .setCallbacks(
        redirects = Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "mail@yourcompany.com",
            notificationUrl = "https://yourstore.com",
        ),
    )
    .build()
    .execute(longPollingConfig) { result ->
        // handling results
    }
```

#### BLIK Alias Payment

If you have for example a returning users and you want to make their payments with BLIK even
smoother,
you can register BLIK Alias for them, so they will only be prompted to accept payment in their
banking app,
without need of entering BLIK code each time they want to make the payment.

In order to do that, you have to use `setBLIKCodeAndRegisterAlias` method instead of `setBLIKCode`.

> [!warning]
> To properly register alias in sandbox, use `amount = 0.15`.

```kotlin
BLIKPayment.Builder()
    .setBLIKCodeAndRegisterAlias(
        code = "777462",
        blikAlias = BlikAlias.Registered(
            value = "1234",
            label = "blik_no_1234",
        ),
    )
// rest of the BLIKPayment configuration
```

If the payment were successful, you can assume an alias was created and can be used for the future
payments.
Next time you want to pay with only an alias, just use `setBLIKAlias` method.

```kotlin
BLIKPayment.Builder()
    .setBLIKAlias(
        BlikAlias.NotRegistered(
            value = "1234",
            label = "blik_no_1234",
        )
    )
// rest of the BLIKPayment configuration
```

#### BLIK Ambiguous Alias Payment

Sometimes, there is a possibility for one alias to be registered more than once. For example, if
you register alias associated with one user for the multiple banks.
In such a situation, you have to fetch those aliases from Tpay API and show them to user to let him
choose one for the payment.

In BLIKPayment's call in execute method you can get `CreateBLIKTransactionResult.AmbiguousBlikAlias`
type of result,
that will indicate that the current alias was registered more than once.
This result holds all possible variations of the alias you used to start payment with in `aliases`
field.
You have to simply show them to the user, let him choose, and then use the chosen alias to retry the
payment.

```kotlin
.execute(config) { result ->
    if (result is CreateBLIKTransactionResult.AmbiguousBlikAlias) {
        showAmbiguousAliases(result.aliases)
    }
}
```

> [!warning]
> In such scenario, you have to use different class to make the payment than at the beginning.
> ```kotlin
> BLIKAmbiguousAliasPayment
>     .from(
>         transactionId = "transaction_id",
>         blikAlias = BlikAlias.Registered(
>             value = "1234",
>             label = "blik_no_1234",
>         ),
>         ambiguousAlias = chosenAlias,
>     )
>     .execute(longPollingConfig) {
>         // handle BLIK ambiguous result
>     } 
> ```

> [!important]
> Right now, Tpay SDK does NOT support recurring payments with BLIK
> In order to achieve that, check
> our [API support for BLIK recurring payments](https://docs-api.tpay.com/en/payment-methods/blik/#blik-recurring-payments).

### Screenless Transfer Payment

Tpay SDK allows you to make transfer payments with bank available to your merchant account.

> [!tip]
> To get banks with their channel ids check
> the [Get Payment Channels](https://docs-api.tpay.com/en/first-steps/list-of-payment-methods/)
> section.

After your customer chooses their bank from the list, you can use it's `channelId` to make the payment.

```kotlin
TransferPayment.Builder()
    .setChannelId(14)
    .setPayer(
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001"
            ),
        ),
    )
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL
        ),
    )
    .setCallbacks(
        Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@yourcompany.com",
            notificationUrl = "https://yourstore.com",
        )
    )
    .build()
    .execute(longPollingConfig) { result ->
        // handle payment result
    }
```

> [!warning]
> If TransferPayment returns `Created` result, you have to handle `paymentUrl`
> sent with it and redirect user to it in order to complete the payment.

### Screenless Installment Payments

Tpay SDK allows you to create long term installment payments.

```kotlin
PekaoInstallmentPayment.Builder()
    .setChannelId(77)
    .setPayer(
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001"
            ),
        ),
    )
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL
        ),
    )
    .setCallbacks(
        Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@yourcompany.com",
            notificationUrl = "https://yourstore.com",
        )
    )
    .build()
    .execute(longPollingConfig) { result ->
        // handle payment result
    }
```

> [!warning]
> If PekaoInstallmentPayment returns `Created` result, you have to handle `paymentUrl`
> sent with it and redirect user to it in order to complete the payment.

### Screenless Deferred Payments

Tpay SDK allows you to create deferred payments (BNPL) using PayPo method.

> [!warning]
> For PayPo payment to work, amount of the payment must be at least 40PLN!
> For more information about PayPo payments
> check [our PayPo documentation](https://docs-api.tpay.com/en/payment-methods/bnpl/#paypo).

> [!tip]
> For sandbox, working phone number is `500123456`

```kotlin
PayPoPayment.Builder()
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 40.0,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL
        ),
    )
    .setPayer(
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001"
            ),
        )
    )
    .setCallbacks(
        Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@yourcompany.com",
            notificationUrl = "https://yourstore.com",
        )
    )
    .build()
    .execute(longPollingConfig) {
        // handle payment result
    }
```

> [!warning]
> If PayPoPayment returns `Created` result, you have to handle `paymentUrl`
> sent with it and redirect user to it in order to complete the payment.

### Screenless Google Pay Payment

Tpay SDK allows you to perform Google Pay transactions.

```kotlin
GooglePayPayment.Builder()
    .setGooglePayToken("google_pay_token")
    .setPayer(
        payer = Payer(
            name = "John Doe",
            email = "john.doe@test.pl",
            phone = "123456789",
            address = Payer.Address(
                city = "Warsaw",
                countryCode = "PL",
                address = "Test Street 1",
                postalCode = "00-001"
            ),
        ),
    )
    .setPaymentDetails(
        paymentDetails = PaymentDetails(
            amount = 5.21,
            description = "description",
            hiddenDescription = "hidden test",
            language = Language.PL
        ),
    )
    .setCallbacks(
        Redirects(
            successUrl = "https://yourstore.com/success",
            errorUrl = "https://yourstore.com/error",
        ),
        notifications = Notifications(
            notificationEmail = "email@yourcompany.com",
            notificationUrl = "https://yourstore.com",
        )
    )
    .build()
    .execute(longPollingConfig) { result ->
        // handle payment result
    }
```

> [!warning]
> If GooglePayPayment returns `Created` result, you have to handle `paymentUrl`
> sent with it and redirect user to it in order to complete the payment.

> [!warning]
> Take under consideration, that choosing this option,
> you have to configure whole Google Wallet SDK and fetch Google Pay token on your own.
> Down below we provide a bit smoother way of handling Google Pay transactions with our wrappers.

#### Google Pay Utils

If you do not want to configure whole Google Pay functionality, you can use `GooglePlayUtil` class.
It will handle all payments, with additional info in the bottom sheet and send you all the needed info in
callback.

> [!warning]
> Before you use our utils, make sure Google Pay is enabled in the device. Use `GooglePayUtil` ->
`checkIfGooglePayIsAvailable` method.

```kotlin
val googlePayUtil = GooglePayUtil(
    activity = requireActivity(),
    googlePayRequest = GooglePayRequest(
        price = 5.21,
        merchantName = "Your Store",
        merchantId = "merchant_id",
    ),
    googlePayEnvironment = GooglePayEnvironment.TEST,
)

// Open the Google Pay module with data specified in GooglePayRequest
googlePayUtil.openGooglePay()
```

To successfully handle result of the payment, implement `handleActivityResult` method in your
activity's `onActivityResult`.

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayUtil.handleActivityResult(requestCode, resultCode, data) { result ->
        when (result) {
            is OpenGooglePayResult.Success -> {
                // handle success
            }
            is OpenGooglePayResult.Cancelled -> {
                // handle cancellation
            }
            is OpenGooglePayResult.UnknownError -> {
                // handle error
            }
        }
    }
}
```

> [!warning]
> If `OpenGooglePayResult` returns `Success`, you **HAVE TO** use it's content to make an actual
> payment by using `GooglePayPayment.Builder`.

## License

This library is released under the [MIT License](https://opensource.org/license/mit/).
