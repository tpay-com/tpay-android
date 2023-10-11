package com.tpay.sdk.internal.addCard

import com.tpay.sdk.R
import com.tpay.sdk.api.PayCardEncryptor
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.designSystem.textfields.CreditCardDate
import com.tpay.sdk.extensions.*
import com.tpay.sdk.extensions.Observable
import com.tpay.sdk.extensions.isValidCVVCode
import com.tpay.sdk.extensions.isValidCreditCardNumber
import com.tpay.sdk.extensions.isValidEmailAddress
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.webView.WebUrl
import com.tpay.sdk.server.dto.parts.PayerDTO
import com.tpay.sdk.server.dto.parts.PayerUrlDTO
import com.tpay.sdk.server.dto.request.CardTokenizationRequestDTO
import com.tpay.sdk.server.dto.response.CardTokenizationResponseDTO


internal class AddCardViewModel : BaseViewModel() {
    internal val nameSurnameError = Observable<FormError>(FormError.None)
    internal val emailError = Observable<FormError>(FormError.None)
    internal val creditCardNumberError = Observable<FormError>(FormError.None)
    internal val expirationDateError = Observable<FormError>(FormError.None)
    internal val cvvError = Observable<FormError>(FormError.None)

    internal val payer = repository.tokenization.payer

    internal val environment: Environment
        get() = configuration.environment

    private val errorObservables = listOf(
        nameSurnameError,
        emailError,
        creditCardNumberError,
        expirationDateError,
        cvvError
    )
    internal val isNFCEnabled = Observable(false)
    internal val shouldReadPayCardData = Observable(false)
    internal val wasNFCScanSuccessful = Observable(false)

    var nameSurname = payer.name
    var email = payer.email
    var creditCardNumber = ""
    var expirationDate = ""
    var cvv = ""

    private var cardTokenizationRequestDTO: CardTokenizationRequestDTO

    init {
        repository.run {
            configuration.merchant?.authorization?.run {
                setAuth(this, configuration.environment)
            }

            cardTokenizationRequestDTO = CardTokenizationRequestDTO().apply {
                callbackUrl = tokenization.notificationUrl
                redirects = PayerUrlDTO().apply {
                    success = internalRedirects.successUrl
                    error = internalRedirects.errorUrl
                }
            }
        }
    }

    fun onSaveCardButtonClick() {
        repository.tokenizationId = null
        screenClickable.value = false
        buttonLoading.value = true

        val creditCardDate = CreditCardDate.from(expirationDate)

        nameSurnameError.value = when {
            nameSurname.isBlank() -> FormError.Resource(R.string.field_required)
            !nameSurname.isValidFirstAndLastName() -> FormError.Resource(R.string.first_last_name_invalid)
            !nameSurname.isFirstAndLastNameLengthValid -> FormError.Resource(R.string.invalid_number_of_characters)
            else -> FormError.None
        }
        emailError.value = when {
            email.isBlank() -> FormError.Resource(R.string.field_required)
            !email.isValidEmailAddress() -> FormError.Resource(R.string.email_address_not_valid)
            else -> FormError.None
        }
        creditCardNumberError.value = when {
            creditCardNumber.isBlank() -> FormError.Resource(R.string.field_required)
            !creditCardNumber.isValidCreditCardNumber() -> FormError.Resource(R.string.card_number_not_valid)
            else -> FormError.None
        }
        expirationDateError.value = when {
            expirationDate.isBlank() -> FormError.Resource(R.string.field_required)
            creditCardDate == null || !creditCardDate.isValid() -> FormError.Resource(R.string.credit_card_date_not_valid)
            else -> FormError.None
        }
        cvvError.value = when {
            cvv.isBlank() -> FormError.Resource(R.string.field_required)
            !cvv.isValidCVVCode() -> FormError.Resource(R.string.card_cvv_number_not_valid)
            else -> FormError.None
        }
        if (areFieldsValid()) {
            val encryptedCardData = PayCardEncryptor(
                configuration
                    .sslCertificatesProvider
                    ?.apiConfiguration
                    ?.publicKeyHash ?: ""
            )
                .encrypt(
                    cardNumber = creditCardNumber,
                    expirationDate = expirationDate,
                    cvv = cvv,
                    domain = configuration.sslCertificatesProvider?.apiConfiguration?.pinnedDomain ?: EXAMPLE_DOMAIN
                )

            cardTokenizationRequestDTO.apply {
                payer = PayerDTO().apply {
                    name = nameSurname
                    email = this@AddCardViewModel.email
                }
                card = encryptedCardData
            }

            repository
                .tokenizeCard(cardTokenizationRequestDTO)
                .observe(this::handleTokenizationSuccess, this::handleError)
        } else {
            screenClickable.value = true
            buttonLoading.value = false
        }
    }

    private fun handleTokenizationSuccess(cardTokenizationResponse: CardTokenizationResponseDTO) {
        cardTokenizationResponse.run {
            if (result == RESULT_SUCCESS) {
                url?.let { tokenizationUrl ->
                    repository.run {
                        webUrl = WebUrl.Tokenization(tokenizationUrl)
                        tokenizationId = id
                    }
                    moveToWebViewScreen()
                } ?: moveToSuccessScreen()
            } else {
                moveToFailureScreen(addToBackStack = true)
            }
        }

        screenClickable.value = true
        buttonLoading.value = false
    }

    private fun areFieldsValid(): Boolean {
        return errorObservables.all { it.value == FormError.None }
    }

    companion object {
        private const val EXAMPLE_DOMAIN = "example.com"
        private const val RESULT_SUCCESS = "success"
    }
}