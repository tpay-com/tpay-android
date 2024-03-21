package com.tpay.sdk.internal.payerData

import android.graphics.drawable.BitmapDrawable
import com.tpay.sdk.R
import com.tpay.sdk.api.screenless.channelMethods.AvailablePaymentMethods
import com.tpay.sdk.api.screenless.channelMethods.ChannelMethod
import com.tpay.sdk.api.screenless.channelMethods.GetPaymentChannels
import com.tpay.sdk.api.screenless.channelMethods.GetPaymentChannelsResult
import com.tpay.sdk.api.screenless.channelMethods.GroupedPaymentChannels
import com.tpay.sdk.extensions.*
import com.tpay.sdk.internal.FormError
import com.tpay.sdk.internal.base.BaseViewModel
import com.tpay.sdk.internal.model.AvailableMethods
import com.tpay.sdk.internal.model.MethodWithImage
import com.tpay.sdk.internal.paymentMethod.PaymentMethodFragment


internal class PayerDataViewModel : BaseViewModel() {
    internal val nameSurnameError = Observable<FormError>(FormError.None)
    internal val emailError = Observable<FormError>(FormError.None)
    var nameSurname = ""
    var email = ""

    init {
        repository.run {
            configuration.merchant?.authorization?.run {
                setAuth(this, configuration.environment)
            }
            transaction.payerContext.payer.run {
                nameSurname = name
                this@PayerDataViewModel.email = email
            }
        }
    }

    fun onSelectPaymentMethodButtonClick() {
        isRequestInProgress = true
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

        if (emailError.value == FormError.None && nameSurnameError.value == FormError.None) {
            repository.transaction.payerContext.payer.run {
                name = nameSurname
                email = this@PayerDataViewModel.email
            }

            GetPaymentChannels().execute(this::handlePaymentChannelsResult)
        } else {
            isRequestInProgress = false
        }
    }

    private fun handlePaymentChannelsResult(result: GetPaymentChannelsResult) {
        when (result) {
            is GetPaymentChannelsResult.Success -> {
                val grouped = GroupedPaymentChannels.from(result.channels)
                val availablePaymentMethods = AvailablePaymentMethods.from(
                    grouped,
                    configuration.paymentMethods,
                    repository.transaction.amount
                )

                val channelsToGetImages = mutableMapOf<String, List<ChannelMethod>>()

                if (availablePaymentMethods.availableTransfers.isNotEmpty()) {
                    channelsToGetImages[TRANSFER_TAG] = availablePaymentMethods.availableTransfers
                }

                if (availablePaymentMethods.availablePekaoInstallmentMethods.isNotEmpty()) {
                    channelsToGetImages[PEKAO_TAG] = availablePaymentMethods.availablePekaoInstallmentMethods
                }

                if (channelsToGetImages.isEmpty()) {
                    repository.availableMethods = AvailableMethods(
                        creditCard = availablePaymentMethods.creditCardMethod,
                        blik = availablePaymentMethods.blikMethod,
                        wallets = availablePaymentMethods.availableWallets,
                        transfers = emptyList(),
                        pekaoInstallments = emptyList()
                    )
                    navigation.changeFragment(PaymentMethodFragment(), addToBackStack = true)
                    screenClickable.value = true
                } else {
                    getMethodsWithImages(channelsToGetImages) { imagesResult ->
                        val transfers = imagesResult[TRANSFER_TAG] ?: emptyList()
                        val pekaoInstallments = imagesResult[PEKAO_TAG] ?: emptyList()

                        repository.availableMethods = AvailableMethods(
                            creditCard = availablePaymentMethods.creditCardMethod,
                            blik = availablePaymentMethods.blikMethod,
                            wallets = availablePaymentMethods.availableWallets,
                            transfers = transfers,
                            pekaoInstallments = pekaoInstallments
                        )

                        navigation.changeFragment(PaymentMethodFragment(), addToBackStack = true)
                        screenClickable.value = true
                    }
                }
            }
            is GetPaymentChannelsResult.Error -> {
                showSomethingWentWrong()
            }
        }
    }

    private fun getMethodsWithImages(
        data: Map<String, List<ChannelMethod>>,
        onFinish: (Map<String, List<MethodWithImage>>) -> Unit
    ) {
        val taggedChannels = mutableListOf<TaggedChannel>()
        data.keys.forEach { key ->
            data[key]?.forEach { channelMethod ->
                taggedChannels.add(TaggedChannel(key, channelMethod))
            }
        }

        val resultMap = mutableMapOf<String, List<MethodWithImage>>()

        taggedChannels
            .map { tagged -> tagged to repository.getImageDrawable(tagged.method.imageUrl) }
            .observe { result ->
                result
                    .groupBy { (tag, _) -> tag }
                    .map { entry ->
                        val tag = entry.key
                        val methodsWithImages = entry.value.map { (_, method) -> method }

                        resultMap[tag] = methodsWithImages
                    }

                onFinish(resultMap)
            }
    }

    private fun List<Pair<TaggedChannel, Completable<BitmapDrawable>>>.observe(block: (List<Pair<String, MethodWithImage>>) -> Unit) {
        val result = mutableListOf<Pair<String, MethodWithImage>>()
        forEach { (taggedChannel, imageCompletable) ->
            imageCompletable.observe({ bitmap ->
                result.add(
                    taggedChannel.tag to MethodWithImage(
                        channelId = taggedChannel.method.channelId,
                        name = taggedChannel.method.name,
                        image = bitmap
                    )
                )
                if (result.size == size) {
                    block(result)
                }
            }, {
                result.add(
                    taggedChannel.tag to MethodWithImage(
                        channelId = taggedChannel.method.channelId,
                        name = taggedChannel.method.name,
                        image = null
                    )
                )
                if (result.size == size) {
                    block(result)
                }
            })
        }
    }

    fun onFragmentDestroy(){
        disposeBag.disposeAll()
    }

    data class TaggedChannel(
        val tag: String,
        val method: ChannelMethod
    )

    companion object {
        private const val TRANSFER_TAG = "transfers"
        private const val PEKAO_TAG = "pekao"
    }
}