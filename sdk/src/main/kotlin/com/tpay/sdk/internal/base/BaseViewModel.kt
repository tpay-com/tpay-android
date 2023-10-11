package com.tpay.sdk.internal.base

import androidx.lifecycle.ViewModel
import com.tpay.sdk.R
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.DisposeBag
import com.tpay.sdk.extensions.Observable
import com.tpay.sdk.internal.*
import com.tpay.sdk.internal.LanguageSwitcher
import com.tpay.sdk.internal.Navigation
import com.tpay.sdk.internal.PaymentCoordinators
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.failureStatus.FailureStatusFragment
import com.tpay.sdk.internal.successStatus.SuccessStatusFragment
import com.tpay.sdk.internal.webView.WebViewFragment
import com.tpay.sdk.server.NoInternetException
import java.lang.Exception
import javax.inject.Inject

internal open class BaseViewModel @Inject constructor(
) : ViewModel() {
    @Inject
    protected lateinit var configuration: Configuration

    @Inject
    protected lateinit var repository: Repository

    @Inject
    protected lateinit var navigation: Navigation

    @Inject
    protected lateinit var disposeBag: DisposeBag

    @Inject
    protected lateinit var paymentCoordinators: PaymentCoordinators

    @Inject
    protected lateinit var addCardCoordinator: AddCardCoordinator

    @Inject
    protected lateinit var languageSwitcher: LanguageSwitcher

    internal val screenClickable = Observable(true)
    internal val buttonLoading = Observable(false)
    internal val errorMessageId = Observable(-1)
    internal val errorMessage = Observable("")

    init {
        injectFields()
    }

    fun handleError(exception: Exception){
        when(exception){
            is NoInternetException -> {
                errorMessageId.value = R.string.no_internet_access
            }
            else -> {
                errorMessageId.value = R.string.something_went_wrong
            }
        }
        buttonLoading.value = false
    }

    protected fun moveToWebViewScreen(){
        navigation.changeFragment(WebViewFragment(), addToBackStack = true)
    }

    protected fun moveToSuccessScreen(){
        navigation.changeFragment(SuccessStatusFragment(), addToBackStack = false)
    }

    protected fun moveToFailureScreen(addToBackStack: Boolean = false){
        navigation.changeFragment(FailureStatusFragment(), addToBackStack = addToBackStack)
    }

    var isRequestInProgress: Boolean = false
        set(value) {
            screenClickable.value = !value
            buttonLoading.value = value
            field = value
        }
}