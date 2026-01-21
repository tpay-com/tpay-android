package com.tpay.sdk.internal

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tpay.sdk.R
import com.tpay.sdk.api.addCard.AddCardDelegate
import com.tpay.sdk.api.models.Compatibility
import com.tpay.sdk.api.payment.PaymentDelegate
import com.tpay.sdk.api.webView.WebViewCallback
import com.tpay.sdk.cache.DirectoryManager
import com.tpay.sdk.databinding.FragmentSheetBinding
import com.tpay.sdk.designSystem.buttons.ButtonLanguage
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.*
import com.tpay.sdk.extensions.Observable
import com.tpay.sdk.internal.addCard.AddCardFragment
import com.tpay.sdk.internal.cardTokenPayment.TokenPaymentProcessingFragment
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.failureStatus.FailureStatusFragment
import com.tpay.sdk.internal.payerData.PayerDataFragment
import com.tpay.sdk.internal.paymentMethod.PaymentMethodFragment
import com.tpay.sdk.internal.processingPayment.ProcessingPaymentFragment
import com.tpay.sdk.internal.successStatus.SuccessStatusFragment
import com.tpay.sdk.internal.webView.WebViewFragment
import com.tpay.sdk.internal.webViewModule.WebViewCoordinator
import com.tpay.sdk.internal.webViewModule.WebViewModuleFragment
import java.util.*
import javax.inject.Inject


internal class SheetFragment : Fragment(R.layout.fragment_sheet) {
    internal val binding: FragmentSheetBinding by viewBinding(FragmentSheetBinding::bind)
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    internal val onSlide = Observable(0f)
    internal val viewModel: SheetViewModel by viewModels()

    @Inject
    internal lateinit var navigation: Navigation

    @Inject
    internal lateinit var repository: Repository

    @Inject
    internal lateinit var languageSwitcher: LanguageSwitcher

    @Inject
    internal lateinit var configuration: Configuration

    @Inject
    internal lateinit var paymentCoordinators: PaymentCoordinators

    @Inject
    internal lateinit var addCardCoordinator: AddCardCoordinator

    @Inject
    internal lateinit var webViewCoordinator: WebViewCoordinator

    @Inject
    internal lateinit var directoryManager: DirectoryManager

    @Inject
    internal lateinit var activityResultHandler: ActivityResultHandler

    private val screenMetrics by lazy { getScreenMetrics() }

    private var savedSoftInputMode: Int? = null

    init {
        injectFields()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageInsets(view)
        handleRestore(savedInstanceState)

        navigation.init(childFragmentManager)
        val backstackFragments = childFragmentManager.fragments

        val startingFragment = when (sheetType) {
            SheetType.TOKENIZATION -> handleTokenizationFlowScreen(backstackFragments)
            SheetType.TOKEN_PAYMENT -> handleTokenPaymentFlowScreen(backstackFragments)
            SheetType.PAYMENT -> handlePaymentFlowScreen(backstackFragments)
            SheetType.WEB_VIEW -> handleWebViewModuleFlowScreen(backstackFragments)
        }

        navigation.changeFragment(startingFragment, addToBackStack = false)

        try {
            initBottomSheet(savedInstanceState)
            showSheet()
            setUpOnBackgroundClick()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun manageInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView.findViewById<ViewGroup>(R.id.coordinatorLayout)) { v, insets ->
            val insetsMask =
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            val requiredInsets = insets.getInsets(insetsMask)
            val tempLayoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            tempLayoutParams.setMargins(
                requiredInsets.left,
                requiredInsets.top,
                requiredInsets.right,
                requiredInsets.bottom
            )
            v.layoutParams = tempLayoutParams
            insets
        }
    }

    /**
     * Clicking on the background of WebView or actual TokenPaymentProcessingFragment
     * should close **NOT** the sheet
     */
    private fun setUpOnBackgroundClick() {
        binding.coordinatorLayout.onClick {
            when (childFragmentManager.fragments.lastOrNull()) {
                is PayerDataFragment,
                is PaymentMethodFragment,
                is AddCardFragment ->
                    closeSheet()

                else -> Unit
            }
        }
    }

    fun handleRestore(savedInstanceState: Bundle?) {
        directoryManager.init(requireContext())
        savedInstanceState?.run { restoreState() } ?: saveState()
        Language.fromConfiguration(configuration.supportedLanguages)
    }

    private fun restoreState() = viewModel.run {
        readConfigurationFromState()
        readRepositoryFromState()
    }

    private fun saveState() = viewModel.run {
        saveConfigurationToState()
        saveRepositoryToState()
    }

    val sheetType: SheetType
        get() = SheetType.values()[arguments?.getInt(SHEET_TYPE_ARGUMENT_NAME)
            ?: throw IllegalStateException()]

    private fun handleWebViewModuleFlowScreen(backstackFragments: List<Fragment>): Fragment {
        return backstackFragments.firstOrNull { fragment -> fragment is WebViewModuleFragment }
            ?: WebViewModuleFragment().apply {
                arguments = this@SheetFragment.arguments
            }
    }

    private fun handleTokenPaymentFlowScreen(backstackFragments: List<Fragment>): Fragment {
        return backstackFragments.run {
            when {
                containsInstanceOf<SuccessStatusFragment>() -> SuccessStatusFragment()
                containsInstanceOf<FailureStatusFragment>() -> FailureStatusFragment()
                containsInstanceOf<WebViewFragment>() -> WebViewFragment()
                else -> TokenPaymentProcessingFragment()
            }
        }
    }

    private fun handleTokenizationFlowScreen(backstackFragments: List<Fragment>): Fragment {
        return backstackFragments.run {
            when {
                containsInstanceOf<SuccessStatusFragment>() -> SuccessStatusFragment()
                containsInstanceOf<FailureStatusFragment>() -> FailureStatusFragment()
                containsInstanceOf<WebViewFragment>() -> WebViewFragment()
                else -> AddCardFragment()
            }
        }
    }

    private fun handlePaymentFlowScreen(backstackFragments: List<Fragment>): Fragment {
        return backstackFragments.run {
            when {
                containsInstanceOf<SuccessStatusFragment>() -> SuccessStatusFragment()
                containsInstanceOf<FailureStatusFragment>() -> FailureStatusFragment()
                containsInstanceOf<WebViewFragment>() -> WebViewFragment()
                containsInstanceOf<ProcessingPaymentFragment>() -> ProcessingPaymentFragment()
                containsInstanceOf<PaymentMethodFragment>() -> PaymentMethodFragment()
                else -> PayerDataFragment()
            }
        }
    }

    private fun initBottomSheet(savedInstanceState: Bundle?) =
        configuration.compatibility.let { compatibility ->
            fun initNative() {
                val screenHeight = requireActivity().screenHeight
                bottomSheetBehavior?.peekHeight =
                    (screenHeight * STANDARD_SHEET_SCREEN_RATIO).toInt()
                binding.bottomSheetContainer.run {
                    layoutParams = layoutParams.apply {
                        height = (screenHeight * STANDARD_SHEET_SCREEN_RATIO).toInt() - 60.px
                    }
                }
            }

            fun initFlutter() = screenMetrics.run {
                val statusBarToScreenRatio =
                    statusBarHeight / screenHeightWithoutBottomBar.toFloat()
                bottomSheetBehavior?.maxHeight =
                    (screenHeightWithoutBottomBar * (1 - statusBarToScreenRatio)).toInt()
                bottomSheetBehavior?.peekHeight =
                    (screenHeightWithoutBottomBar * STANDARD_SHEET_SCREEN_RATIO).toInt()
                binding.bottomSheetContainer.run {
                    layoutParams = layoutParams.apply {
                        height =
                            (screenHeightWithoutBottomBar * STANDARD_SHEET_SCREEN_RATIO).toInt() - 60.px
                    }
                }
            }

            bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
                isHideable = true
            }

            when (compatibility) {
                Compatibility.NATIVE -> initNative()
                Compatibility.FLUTTER, Compatibility.REACT_NATIVE -> initFlutter()
            }

            navigation.fragmentListeners({
                handleSheetStateChange(bottomSheetBehavior?.state)
            }, {
                handleSheetStateChange(bottomSheetBehavior?.state)
            })

            bottomSheetBehavior?.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    handleSheetStateChange(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset >= 0) {
                        handleDraggingAndSettling()
                        onSlide.value = slideOffset
                    }
                }
            })

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

            binding.run {
                headerLbl.setInAnimation(context, R.anim.alpha_in)
                headerLbl.setOutAnimation(context, R.anim.alpha_out)

                closeBtn.onClick {
                    closeSheet()
                }
                userCard.onClick {
                    activity?.onBackPressed()
                }

                languageBtn.languageChangeListener =
                    object : ButtonLanguage.LanguageChangeListener {
                        override fun onChange(language: Language) {
                            viewModel.languageSelectedByUser = language
                            languageSwitcher.setLanguage(language)
                        }
                    }

                languageSwitcher.setLanguage(
                    if (savedInstanceState == null) Language.from(configuration.preferredLanguage)
                    else viewModel.languageSelectedByUser
                )

                try {
                    val selectedLocale =
                        languageSwitcher.localeObservable.value ?: Locale.getDefault()
                    languageBtn.language = if (selectedLocale == Locale.getDefault()) {
                        getLanguageForLocale(Locale.getDefault())
                    } else {
                        getLanguageForLocale(selectedLocale)
                    }
                } catch (exception: Exception) {
                    languageBtn.language = Language.ENGLISH
                }
            }

            showLanguageBtn()
        }

    private fun getContainerHeightFor(fragment: Fragment?): Int = binding.run {
        with(bottomSheet){
            height - top - if (shouldBottomBarBeDisplayed(fragment)) 60.px else 0.px
        }
    }

    private fun handleSheetStateChange(state: Int?) {
        try {
            val currentFragment = navigation.getCurrentFragment()
            if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_EXPANDED) {
                binding.bottomSheetContainer.run {
                    layoutParams = layoutParams.apply {
                        height = getContainerHeightFor(currentFragment)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun shouldBottomBarBeDisplayed(fragment: Fragment?): Boolean {
        return fragment !is WebViewFragment &&
                fragment !is SuccessStatusFragment &&
                fragment !is FailureStatusFragment &&
                fragment !is WebViewModuleFragment
    }


    fun handleDraggingAndSettling() {
        try {
            val currentFragment = navigation.getCurrentFragment()
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_DRAGGING || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_SETTLING) {
                binding.bottomSheetContainer.run {
                    layoutParams = layoutParams.apply {
                        height = getContainerHeightFor(currentFragment)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun setSheetFullscreen() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior?.isDraggable = false
    }

    fun setSheetStandardHeight() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior?.isDraggable = true
    }

    private fun showSheet() {
        requireActivity().run {
            savedSoftInputMode = window.attributes.softInputMode
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }

        binding.bottomSheet.post {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior?.isHideable = false
        }

        binding.dimBackground.isVisible = true
        binding.dimBackground.animate().alpha(1f).setDuration(DIM_BACKGROUND_ANIMATION_DURATION)
            .start()
    }

    fun closeSheet() {
        requireActivity().run {
            hideKeyboard()
            savedSoftInputMode?.let(window::setSoftInputMode)
        }
        bottomSheetBehavior?.isHideable = true
        binding.dimBackground.animate().alpha(0f).setDuration(DIM_BACKGROUND_ANIMATION_DURATION)
            .withEndAction {
                requireActivity().supportFragmentManager.beginTransaction()
                    .remove(this).commit()
                ScreenOrientationUtil.unlock(requireActivity())
            }.start()
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        when (sheetType) {
            SheetType.TOKENIZATION -> addCardCoordinator.moduleClosed.invoke()
            SheetType.WEB_VIEW -> webViewCoordinator.onWebViewClosed.invoke()
            else -> paymentCoordinators.get(sheetType)?.moduleClosed?.invoke()
        }
    }

    fun setHeaderText(text: String, withAnim: Boolean = true) {
        if (withAnim) {
            binding.headerLbl.setText(text)
        } else {
            binding.headerLbl.setCurrentText(text)
        }
    }

    var labelIcon: Drawable?
        get() = binding.labelIcon.drawable
        set(value) {
            binding.labelIcon.run {
                isVisible = if (value == null) {
                    false
                } else {
                    setImageDrawable(value)
                    true
                }
            }
        }

    var isSheetHeaderVisible: Boolean
        get() = binding.headerBackground.isVisible
        set(value) {
            binding.run {
                headerBackground.isVisible = value
                labelIcon.isVisible = value
            }
            isCloseButtonVisible = value
            isHeaderTextVisible = value
        }

    private var isCloseButtonVisible: Boolean
        get() = binding.closeBtn.isVisible
        set(value) {
            binding.closeBtn.isVisible = value
        }

    private var isHeaderTextVisible: Boolean
        get() = binding.headerLbl.isVisible
        set(value) {
            binding.headerLbl.isVisible = value
        }

    fun showErrorMessage(
        message: String,
        withAnim: Boolean = true,
        endAction: (() -> Unit)? = null
    ) {
        binding.errorCard.run {
            if (!isAnimationInProgress) {
                isClickBlockerVisible = true
                if (withAnim) {
                    translationY = ERROR_MESSAGE_START_Y_TRANSLATION.px
                    text = message
                    this
                        .animate()
                        .translationY(ERROR_MESSAGE_END_Y_TRANSLATION.px)
                        .setDuration(ERROR_MESSAGE_ENTER_EXIT_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            startAnimation(ERROR_MESSAGE_DURATION) {
                                hideErrorMessage()
                                endAction?.invoke()
                            }
                        }
                        .start()
                }
                isVisible = true
            }
        }
    }

    private fun hideErrorMessage(withAnim: Boolean = true, endAction: (() -> Unit)? = null) {
        binding.errorCard.run {
            if (!isAnimationInProgress) {
                if (withAnim) {
                    translationY = ERROR_MESSAGE_END_Y_TRANSLATION.px
                    this
                        .animate()
                        .translationY(ERROR_MESSAGE_START_Y_TRANSLATION.px)
                        .setDuration(ERROR_MESSAGE_ENTER_EXIT_DURATION)
                        .setInterpolator(AccelerateInterpolator())
                        .withEndAction {
                            endAction?.invoke()
                            isVisible = false
                            isClickBlockerVisible = false
                        }
                        .start()
                } else {
                    isVisible = false
                }
            }
        }
    }

    fun showLanguageBtn(withAnim: Boolean = true, endAction: (() -> Unit)? = null) {
        if (Language.fromConfiguration.size > 1) {
            if (withAnim) {
                binding.languageBtn.translationY = LANGUAGE_BUTTON_START_TRANSLATION
                binding.languageBtn
                    .animate()
                    .translationY(TRANSLATION_ZERO)
                    .setDuration(LANGUAGE_BUTTON_ANIMATION_DURATION)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        endAction?.invoke()
                    }
                    .start()
            }
            binding.languageBtn.isVisible = true
        }
    }

    fun hideLanguageBtn(withAnim: Boolean = true, endAction: (() -> Unit)? = null) {
        if (withAnim) {
            binding.languageBtn.translationY = TRANSLATION_ZERO
            binding.languageBtn
                .animate()
                .translationY(LANGUAGE_BUTTON_START_TRANSLATION)
                .setDuration(LANGUAGE_BUTTON_ANIMATION_DURATION)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    endAction?.invoke()
                    try {
                        binding.languageBtn.isVisible = false
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
                .start()
        } else {
            binding.languageBtn.isVisible = false
        }
    }

    fun showUserCard(
        withAnim: Boolean = true,
        endAction: (() -> Unit)? = null
    ) {
        binding.userCard.run {
            userName = repository.transaction.payerContext.payer.name
            userEmail = repository.transaction.payerContext.payer.email
            if (withAnim) {
                translationY = USER_CARD_START_TRANSLATION
                animate()
                    .translationY(TRANSLATION_ZERO)
                    .setStartDelay(USER_CARD_SHOW_DELAY)
                    .setDuration(USER_CARD_ANIMATION_DURATION)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        endAction?.invoke()
                    }
                    .start()
            }
            isVisible = true
        }
    }

    fun hideUserCard(withAnim: Boolean = true, endAction: (() -> Unit)? = null) {
        if (withAnim) {
            binding.userCard.translationY = TRANSLATION_ZERO
            binding.userCard
                .animate()
                .translationY(USER_CARD_START_TRANSLATION)
                .setDuration(USER_CARD_ANIMATION_DURATION)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    endAction?.invoke()
                    try {
                        binding.userCard.isVisible = false
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
                .start()
        } else {
            binding.userCard.isVisible = false
        }
    }

    fun addPaymentDelegate(sheetType: SheetType, paymentDelegate: PaymentDelegate) {
        paymentCoordinators.add(
            sheetType,
            PaymentCoordinator(
                paymentCreated = paymentDelegate::onPaymentCreated,
                paymentCompleted = paymentDelegate::onPaymentCompleted,
                paymentCancelled = paymentDelegate::onPaymentCancelled,
                moduleClosed = paymentDelegate::onModuleClosed
            )
        )
    }

    fun addTokenizationDelegate(addCardDelegate: AddCardDelegate) {
        addCardCoordinator.run {
            addCardSuccess = addCardDelegate::onAddCardSuccess
            addCardFailure = addCardDelegate::onAddCardFailure
            moduleClosed = addCardDelegate::onModuleClosed
        }
    }

    fun addWebViewCallback(callback: WebViewCallback) {
        webViewCoordinator.run {
            onPaymentSuccess = {
                callback.onPaymentSuccess()
                closeSheet()
            }
            onPaymentFailure = {
                callback.onPaymentFailure()
                closeSheet()
            }
            onWebViewClosed = callback::onWebViewClosed
        }
    }

    fun activityResultFromRestore(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultHandler.onResult.value = Triple(requestCode, resultCode, data)
    }

    private fun getLanguageForLocale(locale: Locale): Language {
        val languageTag = locale.toLanguageTag().substring(0, 2)
        return Language.values().first { it.languageTag == languageTag }
    }

    var isClickBlockerVisible: Boolean
        get() = binding.clickBlocker.isVisible
        set(value) {
            binding.clickBlocker.isVisible = value
        }

    var isShadowBelowHeaderVisible: Boolean = false
        set(value) {
            binding.shadowView.isInvisible = !value
            field = value
        }

    companion object {
        private const val ERROR_MESSAGE_DURATION = 4000L
        private const val ERROR_MESSAGE_START_Y_TRANSLATION = 100f
        private const val ERROR_MESSAGE_END_Y_TRANSLATION = 0f
        private const val ERROR_MESSAGE_ENTER_EXIT_DURATION = 250L
        private val LANGUAGE_BUTTON_START_TRANSLATION = 60f.px
        private val USER_CARD_START_TRANSLATION = 80f.px
        private val TRANSLATION_ZERO = 0f.px
        private const val USER_CARD_ANIMATION_DURATION = 400L
        private const val USER_CARD_SHOW_DELAY = 300L
        private const val LANGUAGE_BUTTON_ANIMATION_DURATION = 250L
        private const val DIM_BACKGROUND_ANIMATION_DURATION = 100L
        private const val STANDARD_SHEET_SCREEN_RATIO = 0.65f
        private val SHEET_TYPE_ARGUMENT_NAME = SheetType::class.java.simpleName

        fun with(sheetType: SheetType, other: Bundle? = null): SheetFragment {
            return SheetFragment().apply {
                arguments = bundleOf(
                    SHEET_TYPE_ARGUMENT_NAME to sheetType.ordinal
                ).apply {
                    other?.run(::putAll)
                }
            }
        }
    }
}

internal data class ScreenMetrics(
    val statusBarHeight: Int,
    val screenHeightWithoutBottomBar: Int
)

internal enum class SheetType {
    PAYMENT, TOKENIZATION, TOKEN_PAYMENT, WEB_VIEW
}