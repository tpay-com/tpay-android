package com.tpay.sdk.internal.paymentMethod

import android.app.Activity
import android.content.Intent
import com.tpay.sdk.R
import com.tpay.sdk.api.models.BlikAlias
import com.tpay.sdk.api.models.DigitalWallet
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.api.models.TokenizedCard
import com.tpay.sdk.api.screenless.PaymentDetails
import com.tpay.sdk.api.screenless.blik.AmbiguousAlias
import com.tpay.sdk.api.screenless.blik.BLIKAmbiguousAliasPayment
import com.tpay.sdk.api.screenless.blik.BLIKPayment
import com.tpay.sdk.api.screenless.blik.CreateBLIKTransactionResult
import com.tpay.sdk.api.screenless.card.CreateCreditCardTransactionResult
import com.tpay.sdk.api.screenless.card.CreditCard
import com.tpay.sdk.api.screenless.card.CreditCardPayment
import com.tpay.sdk.api.screenless.googlePay.*
import com.tpay.sdk.api.screenless.transfer.CreateTransferTransactionResult
import com.tpay.sdk.api.screenless.transfer.TransferPayment
import com.tpay.sdk.designSystem.textfields.CreditCardDate
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.Language.Companion.asApi
import com.tpay.sdk.internal.SheetType
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.processingPayment.ProcessingPaymentFragment
import com.tpay.sdk.internal.webView.WebUrl

internal class PaymentMethodViewModel : BaseViewModel() {
    internal var screenState = PaymentMethodScreenState.CARD
    internal val cardNumberError = Observable<FormError>(FormError.None)
    internal val cardDateError = Observable<FormError>(FormError.None)
    internal val cardCVVError = Observable<FormError>(FormError.None)
    internal val blikNumberError = Observable<FormError>(FormError.None)
    internal val walletError = Observable(false)
    internal val transferError = Observable(false)
    internal val walletMethod = Observable(WalletMethod.NONE)
    internal val isNfcEnabled = Observable(false)
    internal val shouldReadPayCardData = Observable(false)
    internal val isPayCardScanSuccessful = Observable(false)
    internal val oneClickCardError = Observable(false)
    internal val oneClickBLIKError = Observable(false)

    internal var payCardFieldsValid = false

    internal var cardNumber = ""
    internal var cardDate = ""
    internal var cardCVV = ""
    internal var saveCardChecked = false
    internal var blikOtc = ""
    internal var selectedTransferId: String? = null

    val transactionAmount: Double
        get() = repository.transaction.amount

    val cardAvailable: Boolean
        get() = repository.availableTransactionMethods.cardPaymentAvailable

    val blikAvailable: Boolean
        get() = repository.availableTransactionMethods.blikPaymentAvailable

    val availableWalletMethods
        get() = repository.availableTransactionMethods.wallets

    val isGooglePayInPaymentMethods: Boolean
        get() = repository.availableTransactionMethods.wallets.contains(WalletMethod.GOOGLE_PAY)

    val merchantName: String
        get() = configuration.merchantDetailsProvider?.merchantDisplayName(languageSwitcher.currentLanguage.asApi()) ?: ""

    val merchantRODOUrl: String
        get() = configuration.merchantDetailsProvider?.regulationsLink(languageSwitcher.currentLanguage.asApi()) ?: ""

    val merchantCity: String?
        get() = configuration.merchantDetailsProvider?.merchantCity(languageSwitcher.currentLanguage.asApi())

    private val automaticBlikPaymentMethod: BlikAlias?
        get() = repository.transaction.payerContext.automaticPaymentMethods?.blikAlias

    val automaticCreditCardPaymentMethods: List<TokenizedCard>
        get() = repository.transaction.payerContext.automaticPaymentMethods?.tokenizedCards ?: emptyList()

    val isBLIKOneClickPaymentAvailable: Boolean
        get() = automaticBlikPaymentMethod?.let { blikAlias -> blikAlias is BlikAlias.Registered } ?: false

    val canBlikAliasBeRegistered: Boolean
        get() = automaticBlikPaymentMethod != null

    val isBlikAliasConflict: Boolean
        get() = ambiguousBlikAliases.isNotEmpty() && ambiguousBlikTransactionId != null

    var onAmbiguousBlikAliases = { }
    var onAmbiguousBlikAliasesError = { }
    var ambiguousBlikAliases: List<AmbiguousAlias> = emptyList()
    var ambiguousBlikTransactionId: String? = null
    var isSaveBlikChecked = false
    var saveBlikLabel = ""

    internal val environment: Environment
        get() = configuration.environment

    var selectedTokenizedCard: TokenizedCard? = null
    var currentAmbiguousAlias: AmbiguousAlias? = null

    val availableTransferMethods: List<TransferMethod>
        get() = repository.transferMethods

    private val googlePayRequest = GooglePayRequest(
        price = repository.transaction.amount,
        merchantName = merchantName,
        merchantId = configuration.merchant?.merchantId ?: ""
    )

    lateinit var googlePayUtil: GooglePayUtil

    fun initGooglePayUtil(activity: Activity) {
        googlePayUtil = GooglePayUtil(
            activity,
            googlePayRequest = googlePayRequest,
            googlePayEnvironment = when (environment) {
                Environment.PRODUCTION -> GooglePayEnvironment.PRODUCTION
                Environment.SANDBOX -> GooglePayEnvironment.TEST
            },
            customRequestCode = GooglePayUtil.GOOGLE_PAY_UI_REQUEST_CODE
        )
    }

    internal fun onPayButtonClicked() {
        buttonLoading.value = true
        screenClickable.value = false
        when (screenState) {
            PaymentMethodScreenState.CARD -> {
                val creditCardDate = CreditCardDate.from(cardDate)

                cardNumberError.value = when {
                    cardNumber.isBlank() -> FormError.Resource(R.string.field_required)
                    !cardNumber.isValidCreditCardNumber() -> FormError.Resource(R.string.card_number_not_valid)
                    else -> FormError.None
                }
                cardDateError.value = when {
                    cardDate.isBlank() -> FormError.Resource(R.string.field_required)
                    creditCardDate == null || !creditCardDate.isValid() -> FormError.Resource(R.string.credit_card_date_not_valid)
                    else -> FormError.None
                }
                cardCVVError.value = when {
                    cardCVV.isBlank() -> FormError.Resource(R.string.field_required)
                    !cardCVV.isValidCVVCode() -> FormError.Resource(R.string.card_cvv_number_not_valid)
                    else -> FormError.None
                }

                if (
                    cardNumberError.value == FormError.None &&
                    cardDateError.value == FormError.None &&
                    cardCVVError.value == FormError.None
                ) {
                    payCardFieldsValid = true

                    CreditCardPayment.Builder().apply {
                        setCreditCard(
                            creditCard = CreditCard(
                                cardNumber = cardNumber,
                                expirationDate = cardDate,
                                cvv = cardCVV
                            ),
                            domain = configuration.sslCertificatesProvider?.apiConfiguration?.pinnedDomain ?: EXAMPLE_DOMAIN,
                            saveCard = saveCardChecked
                        )
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute(onResult = this::handleCreditCardResult)
                } else {
                    payCardFieldsValid = false
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
            PaymentMethodScreenState.CARD_ONE_CLICK -> {
                selectedTokenizedCard?.let { tokenizedCard ->
                    oneClickCardError.value = false

                    CreditCardPayment.Builder().apply {
                        setCreditCardToken(tokenizedCard.token)
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute(onResult = this::handleCreditCardResult)
                } ?: kotlin.run {
                    oneClickCardError.value = true
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
            PaymentMethodScreenState.BLIK, PaymentMethodScreenState.BLIK_ONE_CLICK_CODE -> {
                blikNumberError.value = when {
                    blikOtc.isBlank() -> FormError.Resource(R.string.field_required)
                    !blikOtc.isValidBLIKCode() -> FormError.Resource(R.string.blik_code_not_valid)
                    else -> FormError.None
                }
                if (blikNumberError.value == FormError.None) {
                    BLIKPayment.Builder().apply {
                        automaticBlikPaymentMethod?.run {
                            if (isSaveBlikChecked) {
                                setBLIKCodeAndRegisterAlias(
                                    code = blikOtc,
                                    blikAlias = BlikAlias.Registered(
                                        value = value,
                                        label = saveBlikLabel.ifBlank { label }
                                    )
                                )
                            } else {
                                setBLIKCode(blikOtc)
                            }
                        } ?: setBLIKCode(blikOtc)
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute { result ->
                        handleBLIKResult(isCodePayment = true, result = result)
                    }
                } else {
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
            PaymentMethodScreenState.BLIK_AMBIGUOUS -> {
                fun handleBlikAmbiguousAliasError() {
                    screenClickable.value = true
                    buttonLoading.value = false
                    ambiguousBlikTransactionId = null
                    ambiguousBlikAliases = emptyList()
                    errorMessageId.value = R.string.something_went_wrong
                    onAmbiguousBlikAliasesError()
                }

                val ambiguousAlias = currentAmbiguousAlias ?: kotlin.run {
                    oneClickBLIKError.value = true
                    screenClickable.value = true
                    buttonLoading.value = false
                    return
                }

                val blikAlias = automaticBlikPaymentMethod ?: kotlin.run {
                    handleBlikAmbiguousAliasError()
                    return
                }

                val transactionId = ambiguousBlikTransactionId ?: kotlin.run {
                    handleBlikAmbiguousAliasError()
                    return
                }

                oneClickBLIKError.value = false

                BLIKAmbiguousAliasPayment.from(
                    transactionId = transactionId,
                    blikAlias = blikAlias,
                    ambiguousAlias = ambiguousAlias
                ).execute { result ->
                    handleBLIKResult(
                        isCodePayment = false,
                        result = result
                    )
                }
            }
            PaymentMethodScreenState.BLIK_ONE_CLICK -> {
                automaticBlikPaymentMethod?.let { blikAlias ->
                    oneClickBLIKError.value = false

                    BLIKPayment.Builder().apply {
                        setBLIKAlias(blikAlias)
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute { result ->
                        handleBLIKResult(isCodePayment = false, result = result)
                    }
                } ?: kotlin.run {
                    oneClickBLIKError.value = true
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
            PaymentMethodScreenState.WALLET -> {
                walletError.value = walletMethod.value == WalletMethod.NONE
                if (walletError.value == false) {
                    if (walletMethod.value == WalletMethod.GOOGLE_PAY) {
                        googlePayUtil.openGooglePay()
                    } else {
                        screenClickable.value = true
                        buttonLoading.value = false
                    }
                } else {
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
            PaymentMethodScreenState.TRANSFER -> {
                transferError.value = selectedTransferId == null
                if (transferError.value == false) {
                    TransferPayment.Builder().apply {
                        setGroupId(selectedTransferId?.toInt() ?: -1)
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute(onResult = this::handleTransferResult)
                } else {
                    screenClickable.value = true
                    buttonLoading.value = false
                }
            }
        }
    }

    fun handleActivityResult(triple: Triple<Int, Int, Intent?>) {
        val (requestCode, resultCode, data) = triple

        googlePayUtil.handleActivityResult(requestCode, resultCode, data) { result ->
            when (result) {
                is OpenGooglePayResult.Success -> {
                    GooglePayPayment.Builder().apply {
                        setGooglePayToken(result.token)
                        repository.transaction.run {
                            setCallbacks(repository.internalRedirects, notifications)
                            setPayer(payerContext.payer)
                            setPaymentDetails(
                                PaymentDetails(
                                    amount = amount,
                                    description = description,
                                    language = languageSwitcher.currentLanguage.asApi()
                                )
                            )
                        }
                    }.build().execute(onResult = this::handleGooglePayResult)
                }
                is OpenGooglePayResult.Cancelled -> {
                    screenClickable.value = true
                    buttonLoading.value = false
                }
                else -> handleGooglePayDataError()
            }
        }
    }

    private fun handleBLIKResult(
        isCodePayment: Boolean,
        result: CreateBLIKTransactionResult
    ){
        repository.selectedPaymentMethod = PaymentMethod.Blik
        when (result) {
            is CreateBLIKTransactionResult.CreatedAndPaid -> {
                handleTransactionId(result.transactionId)
                moveToSuccessScreen()
            }
            is CreateBLIKTransactionResult.Created -> {
                handleTransactionId(result.transactionId)
                moveToProcessingPaymentScreen(addToBackStack = true)
            }
            is CreateBLIKTransactionResult.ConfiguredPaymentFailed -> {
                if (isCodePayment) {
                    errorMessageId.value = R.string.blik_code_not_found_or_expired
                } else {
                    handleTransactionId(result.transactionId)
                    moveToFailureScreen(addToBackStack = true)
                }
            }
            is CreateBLIKTransactionResult.Error -> {
                result.transactionId?.let(this::handleTransactionId)
                moveToFailureScreen(addToBackStack = true)
            }
            is CreateBLIKTransactionResult.AmbiguousBlikAlias -> {
                ambiguousBlikTransactionId = result.transactionId
                ambiguousBlikAliases = result.aliases
                onAmbiguousBlikAliases()
            }
        }

        val isErrorMessageDisplayed = isCodePayment && result is CreateBLIKTransactionResult.ConfiguredPaymentFailed

        screenClickable.value = !isErrorMessageDisplayed
        buttonLoading.value = false
    }

    private fun handleTransferResult(result: CreateTransferTransactionResult) {
        repository.selectedPaymentMethod = PaymentMethod.Pbl
        when (result) {
            is CreateTransferTransactionResult.Created -> {
                handleTransactionId(result.transactionId)
                handlePaymentUrl(result.paymentUrl)
            }
            is CreateTransferTransactionResult.Error -> {
                result.transactionId?.let(this::handleTransactionId)
                moveToFailureScreen(addToBackStack = true)
            }
        }

        screenClickable.value = true
        buttonLoading.value = false
    }

    private fun handleGooglePayDataError() {
        screenClickable.value = true
        buttonLoading.value = false
        errorMessageId.value = R.string.something_went_wrong
    }

    private fun handleGooglePayResult(result: CreateGooglePayTransactionResult) {
        repository.selectedPaymentMethod = PaymentMethod.DigitalWallets(listOf(DigitalWallet.GOOGLE_PAY))
        when (result) {
            is CreateGooglePayTransactionResult.CreatedAndPaid -> {
                handleTransactionId(result.transactionId)
                moveToSuccessScreen()
            }
            is CreateGooglePayTransactionResult.Created -> {
                handleTransactionId(result.transactionId)
                handlePaymentUrl(result.paymentUrl)
            }
            is CreateGooglePayTransactionResult.Error -> {
                result.transactionId?.let(this::handleTransactionId)
                moveToFailureScreen(addToBackStack = true)
            }
        }

        screenClickable.value = true
        buttonLoading.value = false
    }

    private fun handleCreditCardResult(result: CreateCreditCardTransactionResult) {
        repository.selectedPaymentMethod = PaymentMethod.Card
        when (result) {
            is CreateCreditCardTransactionResult.CreatedAndPaid -> {
                handleTransactionId(result.transactionId)
                moveToSuccessScreen()
            }
            is CreateCreditCardTransactionResult.Created -> {
                handleTransactionId(result.transactionId)
                handlePaymentUrl(result.paymentUrl)
            }
            is CreateCreditCardTransactionResult.Error -> {
                result.transactionId?.let(this::handleTransactionId)
                moveToFailureScreen(addToBackStack = true)
            }
        }

        screenClickable.value = true
        buttonLoading.value = false
    }

    private fun handleTransactionId(id: String) {
        paymentCoordinators.get(SheetType.PAYMENT)?.paymentCreated?.invoke(id)
        repository.transactionId = id
    }

    private fun handlePaymentUrl(url: String) {
        repository.webUrl = WebUrl.Payment(url)
        moveToWebViewScreen()
    }

    private fun moveToProcessingPaymentScreen(addToBackStack: Boolean = false) {
        navigation.changeFragment(ProcessingPaymentFragment(), addToBackStack = addToBackStack)
    }

    internal fun onCheckboxClick() {
        saveCardChecked = !saveCardChecked
    }

    internal fun onTransferItemClick(id: String) {
        selectedTransferId = id
    }

    companion object {
        private const val EXAMPLE_DOMAIN = "example.com"
    }
}