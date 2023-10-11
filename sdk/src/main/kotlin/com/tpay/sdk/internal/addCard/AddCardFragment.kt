package com.tpay.sdk.internal.addCard

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.wallet.*
import com.tpay.sdk.R
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.screenless.googlePay.GooglePayEnvironment
import com.tpay.sdk.databinding.FragmentAddCardBinding
import com.tpay.sdk.designSystem.textfields.TextFieldAbstract
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.LegalNotesToSpan
import com.tpay.sdk.internal.base.BaseFragment
import com.tpay.sdk.internal.nfcScanner.PayCardScanner
import com.tpay.sdk.internal.paymentMethod.Composition
import com.tpay.sdk.internal.paymentMethod.CompositionManager
import java.util.*
import javax.inject.Inject


internal class AddCardFragment : BaseFragment(R.layout.fragment_add_card) {
    override val binding: FragmentAddCardBinding by viewBinding(FragmentAddCardBinding::bind)
    override val viewModel = AddCardViewModel()

    @Inject
    lateinit var payCardScanner: PayCardScanner

    private val compositionManager = CompositionManager()
    private lateinit var modifiedCtx: Context

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var cardRecognitionPendingIntent: PendingIntent

    private val resolutionForOCRResult =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            this::handleOCRScanResult
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modifiedCtx = requireContext()

        paymentsClient = createPaymentsClient(requireActivity())

        checkIfOCRIconShouldBeVisible()

        if (requireContext().isNFCAvailable()) {
            viewModel.isNFCEnabled.value = NfcAdapter.getDefaultAdapter(modifiedCtx).isEnabled
            val intent = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            requireActivity().registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.run {
                        viewModel.run {
                            if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                                val state = getIntExtra(
                                    NfcAdapter.EXTRA_ADAPTER_STATE,
                                    NfcAdapter.STATE_OFF
                                )
                                when (state) {
                                    NfcAdapter.STATE_OFF -> {
                                        isNFCEnabled.value = false
                                        shouldReadPayCardData.value = false
                                        if (compositionManager.currentComposition is NFCScanComposition) {
                                            changeComposition(EnableNFCComposition(binding, modifiedCtx))
                                        }
                                    }
                                    NfcAdapter.STATE_ON -> {
                                        isNFCEnabled.value = true
                                        shouldReadPayCardData.value = true
                                        if (compositionManager.currentComposition is EnableNFCComposition) {
                                            changeComposition(NFCScanComposition(binding, viewModel, modifiedCtx))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCardInformationComposition()
        sheetFragment.run {
            isSheetHeaderVisible = true
            labelIcon = ContextCompat.getDrawable(modifiedCtx, R.drawable.ic_mastercard_visa)
            showLanguageBtn()
        }

        applyLanguage(languageSwitcher.localeObservable.value ?: Locale.getDefault())

        payCardScanner.startScan(requireActivity()) { payCardScanResult ->
            viewModel.run {
                if (shouldReadPayCardData.value!!) {
                    if (payCardScanResult == null) {
                        wasNFCScanSuccessful.value = false
                    } else {
                        val creditCardInformation = payCardScanResult.getCreditCardInformation()
                        creditCardInformation?.run {
                            wasNFCScanSuccessful.value = true
                            runOnMainThread {
                                showCardInformationComposition()
                                binding.addCard.run {
                                    creditCardNumberTextField.notFormattedText = cardNumber
                                    val calendar =
                                        Calendar.getInstance().also { it.time = expirationDate }
                                    creditCardDateTextField.notFormattedText =
                                        "${calendar.month.formatMonth()}${calendar.year.formatYearLast2Digits()}"
                                    binding.nfcScan.tryAgainButton.performClick()
                                }
                            }
                        }
                    }
                }
            }
        }

        setOnClicks()
        observeLanguageChanges()
        observeViewModelFields()
        setupTextFieldValidation()
    }

    private fun handleOCRScanResult(activityResult: ActivityResult) {
        activityResult.data?.let { intent ->
            val data = PaymentCardRecognitionResult.getFromIntent(intent) ?: return@let

            binding.addCard.run {
                creditCardNumberTextField.notFormattedText = data.pan
                creditCardDateTextField.notFormattedText =
                    data.creditCardExpirationDate?.let { "%02d/%d".format(it.month, it.year) } ?: ""
            }
        }
    }

    private fun openGooglePayScanActivity() {
        val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
        paymentsClient
            .getPaymentCardRecognitionIntent(request)
            .addOnSuccessListener { intentResponse ->
                cardRecognitionPendingIntent = intentResponse.paymentCardRecognitionPendingIntent
                val intentSenderRequest =
                    IntentSenderRequest.Builder(cardRecognitionPendingIntent.intentSender).build()
                resolutionForOCRResult.launch(intentSenderRequest)
            }
            .addOnFailureListener {
                binding.addCard.creditCardNumberTextField.isScanIconVisible = false
                sheetFragment.showErrorMessage(requireContext().getString(R.string.something_went_wrong))
            }
    }

    private fun checkIfOCRIconShouldBeVisible(){
        val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
        paymentsClient
            .getPaymentCardRecognitionIntent(request)
            .addOnFailureListener {
                try {
                    binding.addCard.creditCardNumberTextField.isScanIconVisible = false
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
    }

    private fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(
                when (viewModel.environment) {
                    Environment.PRODUCTION -> GooglePayEnvironment.PRODUCTION.actual
                    Environment.SANDBOX -> GooglePayEnvironment.TEST.actual
                }
            )
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    private fun setupTextFieldValidation() {
        binding.addCard.addCardNameSurnameTextField.setInputValidator(object : TextFieldAbstract.InputValidator {
            override fun validate(value: String): String? {
                val stringId = when {
                    !value.isFirstAndLastNameLengthValid -> R.string.invalid_number_of_characters
                    !value.isValidFirstAndLastName() -> R.string.first_last_name_invalid
                    else -> null
                }

                return stringId?.let(modifiedCtx::getString)
            }
        })
    }

    private fun observeLanguageChanges() {
        languageSwitcher.localeObservable.observe(this::applyLanguage)
    }

    private fun applyLanguage(locale: Locale) {
        val ctx = getModifiedContextForLocale(locale)
        modifiedCtx = ctx
        sheetFragment.setHeaderText(ctx.getString(R.string.add_card), withAnim = false)
        binding.run {
            rodoTextView.text = prepareBoldSpannedURLText(
                LegalNotesToSpan.prepareRODOTextsToSpan(ctx, languageSwitcher.currentLanguage),
                ctx
            )
            addCardButton.text = ctx.getString(R.string.save_card)
            addCard.run {
                addCardNameSurnameTextField.errorMessage = null
                addCardEmailTextField.errorMessage = null
                creditCardNumberTextField.errorMessage = null
                creditCardDateTextField.errorMessage = null
                creditCardCVVTextField.errorMessage = null
            }
        }
        compositionManager.currentComposition?.let { composition ->
            when (composition) {
                is CardInformationComposition -> {
                    showCardInformationComposition()
                }
                is EnableNFCComposition -> {
                    changeComposition(EnableNFCComposition(binding, ctx))
                }
                is NFCScanComposition -> {
                    changeComposition(NFCScanComposition(binding, viewModel, ctx))
                }
            }
        }
    }

    private fun observeViewModelFields() {
        viewModel.run {
            binding.run {
                screenClickable.observe { clickable ->
                    sheetFragment.isClickBlockerVisible = !clickable
                }

                buttonLoading.observe { isLoading ->
                    addCardButton.isLoading = isLoading
                }
            }
        }
    }

    private fun setOnClicks() {
        binding.run {
            addCard.creditCardNumberTextField.run {
                onNfcIconClick {
                    if (viewModel.isNFCEnabled.value == true) {
                        changeComposition(NFCScanComposition(binding, viewModel, modifiedCtx))
                    } else {
                        changeComposition(EnableNFCComposition(binding, modifiedCtx))
                    }
                }
                onScanIconClick {
                    openGooglePayScanActivity()
                }
            }

            enableNfc.run {
                enableNfcBackButton.onClick {
                    showCardInformationComposition()
                }
            }
            nfcScan.run {
                cardNFCScanBackButton.onClick {
                    showCardInformationComposition()
                }
            }
        }
    }

    private fun changeComposition(composition: Composition) {
        compositionManager.change(composition)
    }

    private fun showCardInformationComposition() {
        changeComposition(
            CardInformationComposition(
                binding,
                viewModel,
                modifiedCtx,
                languageSwitcher.currentLanguage
            )
        )
    }

    override fun onDestroy() {
        payCardScanner.stopScan(requireActivity())
        super.onDestroy()
        compositionManager.onDestroy()
        languageSwitcher.localeObservable.dispose()
        viewModel.run {
            screenClickable.dispose()
            buttonLoading.dispose()
        }
    }
}

internal var FragmentAddCardBinding.isBottomLayoutVisible: Boolean
    get() = bottomBarLayout.isVisible
    set(value) {
        bottomBarLayout.isVisible = value
    }