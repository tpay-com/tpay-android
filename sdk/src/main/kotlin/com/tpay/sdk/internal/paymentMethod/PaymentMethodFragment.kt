package com.tpay.sdk.internal.paymentMethod

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.gms.wallet.*
import com.tpay.sdk.R
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.screenless.googlePay.GooglePayEnvironment
import com.tpay.sdk.databinding.FragmentPaymentMethodBinding
import com.tpay.sdk.designSystem.textfields.TextFieldAbstract
import com.tpay.sdk.designSystem.textfields.Validators
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.LegalNotesToSpan
import com.tpay.sdk.internal.base.BaseFragment
import com.tpay.sdk.internal.nfcScanner.PayCardScanner
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException


internal class PaymentMethodFragment : BaseFragment(R.layout.fragment_payment_method) {
    override val binding: FragmentPaymentMethodBinding by viewBinding(FragmentPaymentMethodBinding::bind)
    override val viewModel: PaymentMethodViewModel by viewModels()

    @Inject
    lateinit var payCardScanner: PayCardScanner

    private val compositionManager = CompositionManager()

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var cardRecognitionPendingIntent: PendingIntent

    private val resolutionForOCRResult =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            this::handleOCRScanResult
        )

    private val nfcBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.run {
                if (action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                    when (getIntExtra(
                        NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF
                    )) {
                        NfcAdapter.STATE_OFF -> {
                            viewModel.isNfcEnabled.value = false
                            viewModel.shouldReadPayCardData.value = false
                        }
                        NfcAdapter.STATE_ON -> {
                            viewModel.isNfcEnabled.value = true
                            viewModel.shouldReadPayCardData.value = true
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initGooglePayUtil(requireActivity())

        paymentsClient = createPaymentsClient(requireActivity())

        checkIfGooglePayButtonShouldBeVisible()
        checkIfOCRIconShouldBeVisible()

        if (requireContext().isNFCAvailable()) {
            viewModel.isNfcEnabled.value = NfcAdapter.getDefaultAdapter(requireContext()).isEnabled
            val intent = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            requireActivity().registerReceiver(nfcBroadcastReceiver, intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetFragment.run {
            hideLanguageBtn()
            isSheetHeaderVisible = true
            showUserCard()
            setHeaderText(
                this@PaymentMethodFragment.requireContext()
                    .getString(R.string.fragment_payment_method_header),
                withAnim = false
            )
        }

        observeActivityResult()
        setSpannedTexts()
        setOnClicks()
        setPaymentMethodPickerBehaviour()
        showAvailablePaymentMethods()
        observeViewModelFields()
        setupTextFieldValidation()

        viewModel.onAmbiguousBlikAliases = { showBLIKAmbiguousPaymentComposition() }
        viewModel.onAmbiguousBlikAliasesError = { showBLIKOneClickPaymentComposition() }

        payCardScanner.startScan(requireActivity()) { payCardScanResult ->
            if (viewModel.shouldReadPayCardData.value!!) {
                if (payCardScanResult == null) {
                    viewModel.isPayCardScanSuccessful.value = false
                } else {
                    val creditCardInformation = payCardScanResult.getCreditCardInformation()
                    creditCardInformation?.run {
                        viewModel.isPayCardScanSuccessful.value = true
                        runOnMainThread {
                            showCardPaymentComposition()
                            binding.cardPaymentMethod.run {
                                creditCardNumberTextField.notFormattedText = cardNumber
                                val calendar = Calendar.getInstance().also { it.time = expirationDate }
                                creditCardDateTextField.notFormattedText =
                                    "${calendar.month.formatMonth()}${calendar.year.formatYearLast2Digits()}"
                                binding.cardNFCScan.tryAgainButton.performClick()
                            }
                        }
                    }
                }
            }
        }
    }

    private val payButtonPriceText: String
        get() = getString(R.string.paying, viewModel.transactionAmount)

    private val payButtonContinueText: String
        get() = getString(R.string.continue_text)

    private val payButtonPayPoText: String
        get() = getString(R.string.paying_with_paypo)

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
                binding.cardPaymentMethod.creditCardNumberTextField.isScanIconVisible = false
                sheetFragment.showErrorMessage(requireContext().getString(R.string.something_went_wrong))
            }
    }

    private fun handleOCRScanResult(activityResult: ActivityResult) {
        activityResult.data?.let { intent ->
            val data = PaymentCardRecognitionResult.getFromIntent(intent) ?: return@let

            binding.cardPaymentMethod.run {
                creditCardNumberTextField.notFormattedText = data.pan
                creditCardDateTextField.notFormattedText =
                    data.creditCardExpirationDate?.let { "%02d/%d".format(it.month, it.year) } ?: ""
            }
        }
    }

    private fun checkIfOCRIconShouldBeVisible(){
        val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
        paymentsClient
            .getPaymentCardRecognitionIntent(request)
            .addOnFailureListener {
                binding.cardPaymentMethod.creditCardNumberTextField.isScanIconVisible = false
            }
    }

    private fun showAvailablePaymentMethods() {
        val paymentMethodBoxes = mutableListOf<Pair<PaymentMethodScreenState, ViewGroup>>()
        binding.run {
            viewModel.run {
                if (availableWalletMethods.isNotEmpty()) {
                    paymentMethodBoxes.add(PaymentMethodScreenState.WALLET to paymentBoxWallet)
                }
                if (cardAvailable) {
                    paymentMethodBoxes.add(PaymentMethodScreenState.CARD to paymentBoxCard)
                }
                if (blikAvailable) {
                    if (isBLIKOneClickPaymentAvailable){
                        paymentMethodBoxes.add(PaymentMethodScreenState.BLIK_ONE_CLICK to paymentBoxBLIK)
                    } else {
                        paymentMethodBoxes.add(PaymentMethodScreenState.BLIK to paymentBoxBLIK)
                    }
                }
                if (availableTransferMethods.isNotEmpty()) {
                    paymentMethodBoxes.add(PaymentMethodScreenState.TRANSFER to paymentBoxTransfer)
                }
                if (availableRatyPekaoMethods.isNotEmpty()) {
                    paymentMethodBoxes.add(PaymentMethodScreenState.RATY_PEKAO to paymentBoxRatyPekao)
                }
                if (payPoAvailable) {
                    paymentMethodBoxes.add(PaymentMethodScreenState.PAY_PO to paymentBoxPayPo)
                }

                paymentMethodBoxes.forEach { it.second.isVisible = true }
                try {
                    paymentMethodBoxes.first().second.updateMargins(start = PAYMENT_BOX_MARGIN_START)
                    paymentMethodBoxes.last().second.updateMargins(end = PAYMENT_BOX_MARGIN_END)

                    when (paymentMethodBoxes.first().first) {
                        PaymentMethodScreenState.CARD -> {
                            if(viewModel.automaticCreditCardPaymentMethods.isEmpty()){
                                showCardPaymentComposition()
                            } else {
                                showOneClickCardPaymentComposition()
                            }
                        }
                        PaymentMethodScreenState.BLIK_ONE_CLICK -> {
                            showBLIKOneClickPaymentComposition()
                        }
                        PaymentMethodScreenState.BLIK -> {
                            showBLIKCodePaymentComposition()
                        }
                        PaymentMethodScreenState.BLIK_AMBIGUOUS -> {
                            showBLIKAmbiguousPaymentComposition()
                        }
                        PaymentMethodScreenState.PAY_PO -> {
                            showPayPoPaymentComposition()
                        }
                        PaymentMethodScreenState.RATY_PEKAO -> {
                            showRatyPekaoComposition()
                        }
                        PaymentMethodScreenState.TRANSFER -> {
                            changeComposition(
                                TransferPaymentComposition(
                                    binding,
                                    viewModel,
                                    sheetFragment,
                                    payButtonPriceText,
                                    requireContext())
                            )
                        }
                        PaymentMethodScreenState.WALLET -> {
                            showWalletPaymentComposition()
                        }
                        else -> { }
                    }
                } catch (exception: NoSuchElementException) { exception.printStackTrace() }
            }
        }
    }

    private fun setOnClicks() {
        binding.run {
            clearFocusOnTextFields()
            payButton.onClick {
                viewModel.onPayButtonClicked()
            }

            cardPaymentMethod.creditCardNumberTextField.onScanIconClick {
                openGooglePayScanActivity()
            }

            payWithCodeButton.onClick {
                showBLIKCodePaymentComposition(
                    showBackButton = true,
                    onBackButtonClick = {
                        showBLIKOneClickPaymentComposition()
                    }
                )
            }
        }
    }

    private fun observeViewModelFields(){
        binding.run {
            viewModel.run {
                screenClickable.observe { clickable ->
                    sheetFragment.isClickBlockerVisible = !clickable
                }
                buttonLoading.observe { isLoading ->
                    sheetFragment.handleDraggingAndSettling()
                    payButton.isLoading = isLoading
                    if(!isLoading && viewModel.screenState == PaymentMethodScreenState.CARD && viewModel.payCardFieldsValid){
                        root.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeActivityResult() {
        activityResultHandler.onResult.observe(viewModel::handleActivityResult)
    }

    private fun checkIfGooglePayButtonShouldBeVisible() {
        viewModel.googlePayUtil.checkIfGooglePayIsAvailable { isAvailable ->
            try {
                isGooglePayButtonVisible = isAvailable && viewModel.isGooglePayInPaymentMethods
                binding.paymentBoxWallet.isVisible = isGooglePayButtonVisible
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showCardPaymentComposition(onHideKeyboard: () -> Unit = { hideKeyboard() }){
        changeComposition(
            CardPaymentComposition(
                binding,
                viewModel,
                this::showOneClickCardPaymentComposition,
                onHideKeyboard,
                requireContext(),
                payButtonPriceText
            )
        )
    }

    private fun showOneClickCardPaymentComposition(){
        changeComposition(
            OneClickCardPaymentComposition(
                binding,
                viewModel,
                sheetFragment,
                this::showCardPaymentComposition,
                payButtonPriceText,
                requireContext()
            )
        )
    }

    private fun showBLIKCodePaymentComposition(
        showBackButton: Boolean = false,
        onBackButtonClick: () -> Unit = { },
        onHideKeyboard: () -> Unit = { hideKeyboard() }
    ){
        changeComposition(
            BLIKCodePaymentComposition(
                binding,
                viewModel,
                payButtonPriceText,
                requireContext(),
                showBackButton,
                onBackButtonClick,
                onHideKeyboard
            )
        )
    }

    private fun showBLIKOneClickPaymentComposition(){
        changeComposition(
            BLIKOneClickPaymentComposition(binding, viewModel, payButtonPriceText, requireContext())
        )
    }

    private fun showBLIKAmbiguousPaymentComposition(){
        changeCompositionIfDifferent(BLIKAmbiguousComposition(binding, viewModel, payButtonPriceText, requireContext()))
    }

    private fun showPayPoPaymentComposition() {
        changeComposition(PayPoComposition(binding, viewModel, payButtonPayPoText, ::hideKeyboard, requireContext()))
    }

    private fun showWalletPaymentComposition() {
        changeComposition(WalletPaymentComposition(binding, viewModel, payButtonPriceText, requireContext()))
    }

    private fun showRatyPekaoComposition() {
        changeComposition(
            RatyPekaoComposition(
                binding,
                viewModel,
                sheetFragment,
                payButtonContinueText,
                requireContext()
            )
        )
    }

    private fun setupTextFieldValidation() {
        binding.payPoPayment.run {
            nameSurnameTextField.setInputValidator(object : TextFieldAbstract.InputValidator {
                override fun validate(value: String): String? {
                    return Validators.validatePayerName(value)?.let(::getString)
                }
            })
            addressTextField.setInputValidator(object : TextFieldAbstract.InputValidator {
                override fun validate(value: String): String? {
                    return Validators.validatePayerAddress(value)?.let(::getString)
                }
            })
            cityTextField.setInputValidator(object : TextFieldAbstract.InputValidator {
                override fun validate(value: String): String? {
                    return Validators.validatePayerCity(value)?.let(::getString)
                }
            })
        }
    }

    private fun setPaymentMethodPickerBehaviour() {
        binding.run {
            paymentBoxCard.onClick {
                if (viewModel.automaticCreditCardPaymentMethods.isEmpty()){
                    showCardPaymentComposition()
                } else {
                    showOneClickCardPaymentComposition()
                }
            }
            paymentBoxBLIK.onClick {
                viewModel.run {
                    if (isBLIKOneClickPaymentAvailable){
                        if (isBlikAliasConflict) {
                            showBLIKAmbiguousPaymentComposition()
                        } else {
                            showBLIKOneClickPaymentComposition()
                        }
                    } else {
                        showBLIKCodePaymentComposition()
                    }
                }
            }
            paymentBoxWallet.onClick {
                showWalletPaymentComposition()
            }
            paymentBoxTransfer.onClick {
                changeComposition(
                    TransferPaymentComposition(
                        binding,
                        viewModel,
                        sheetFragment,
                        payButtonPriceText,
                        requireContext()
                    )
                )
            }
            paymentBoxRatyPekao.onClick {
                changeComposition(
                    RatyPekaoComposition(
                        binding,
                        viewModel,
                        sheetFragment,
                        payButtonContinueText,
                        requireContext()
                    )
                )
            }
            paymentBoxPayPo.onClick(::showPayPoPaymentComposition)
        }
    }

    private fun changeCompositionIfDifferent(composition: Composition) {
        compositionManager.changeIfDifferent(composition)
        manageLayoutChanges()
    }

    private fun changeComposition(composition: Composition) {
        compositionManager.change(composition)
        manageLayoutChanges()
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

    private var isGooglePayButtonVisible: Boolean
        get() = binding.walletPaymentMethod.googlePay.isVisible
        set(value) {
            binding.walletPaymentMethod.googlePay.isVisible = value
        }

    private fun setSpannedTexts() {
        binding.run {
            regulationsTextView.run {
                text = prepareBoldSpannedURLText(
                    LegalNotesToSpan.prepareRegulationTextsToSpan(
                        requireContext(),
                        languageSwitcher.currentLanguage
                    ),
                    requireContext()
                )
                movementMethod = LinkMovementMethod.getInstance()
            }

            rodoTextView.run {
                text = prepareBoldSpannedURLText(
                    LegalNotesToSpan.prepareRODOTextsToSpan(
                        requireContext(),
                        languageSwitcher.currentLanguage
                    ),
                    requireContext()
                )
                movementMethod = LinkMovementMethod.getInstance()
            }

            cardPaymentMethod.rodoInfo.run {
                text = prepareBoldSpannedURLText(
                    LegalNotesToSpan.prepareMerchantRODOTextsToSpan(
                        requireContext(),
                        viewModel.merchantName,
                        viewModel.merchantRODOUrl,
                        viewModel.merchantCity
                    ), requireContext()
                )
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.saveCardChecked = binding.cardPaymentMethod.saveCardCheckbox.isChecked
        checkIfGooglePayButtonShouldBeVisible()
    }

    override fun onDestroy() {
        activityResultHandler.onResult.dispose()
        compositionManager.onDestroy()
        payCardScanner.stopScan(requireActivity())
        if (requireContext().isNFCAvailable()) {
            requireActivity().unregisterReceiver(nfcBroadcastReceiver)
        }
        super.onDestroy()
    }

    companion object {
        val PAY_BUTTON_MARGIN_BOTTOM = 8.px
        val PAY_BUTTON_MARGIN_BOTTOM_WITH_CODE_BUTTON = 0.px
        private val PAYMENT_BOX_MARGIN_START = 20.px
        private val PAYMENT_BOX_MARGIN_END = 20.px
    }
}

internal fun FragmentPaymentMethodBinding.setPayButtonText(text: String) {
    payButton.text = text
}

internal fun FragmentPaymentMethodBinding.clearFocusOnTextFields() {
    cardPaymentMethod.run {
        creditCardNumberTextField.clearFocus()
        creditCardCVVTextField.clearFocus()
        creditCardDateTextField.clearFocus()
    }
    blikCodePayment.blikTextField.clearFocus()
}

internal var FragmentPaymentMethodBinding.isBottomLayoutVisible: Boolean
    get() = bottomBarLayout.isVisible
    set(value) {
        bottomBarLayout.isVisible = value
        divider.isVisible = value
    }

internal var FragmentPaymentMethodBinding.isPayWithCodeButtonVisible: Boolean
    get() = payWithCodeButton.isVisible
    set(value) {
        payWithCodeButton.isVisible = value
        payButton.updateMargins(bottom = if (value) PaymentMethodFragment.PAY_BUTTON_MARGIN_BOTTOM_WITH_CODE_BUTTON else PaymentMethodFragment.PAY_BUTTON_MARGIN_BOTTOM)
    }