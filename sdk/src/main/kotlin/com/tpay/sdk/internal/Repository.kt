package com.tpay.sdk.internal

import android.graphics.drawable.BitmapDrawable
import com.tpay.sdk.api.addCard.Tokenization
import com.tpay.sdk.api.cardTokenPayment.CardTokenTransaction
import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.models.PaymentMethod
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.api.models.transaction.Transaction
import com.tpay.sdk.api.screenless.Redirects
import com.tpay.sdk.api.screenless.channelMethods.AvailablePaymentMethods
import com.tpay.sdk.cache.Cache
import com.tpay.sdk.cache.CachedNetworkImage
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.Completable
import com.tpay.sdk.extensions.toBitmapDrawable
import com.tpay.sdk.internal.webView.WebUrl
import com.tpay.sdk.server.ImageEmptyException
import com.tpay.sdk.server.ServerService
import com.tpay.sdk.server.dto.request.CardTokenizationRequestDTO
import com.tpay.sdk.server.dto.response.GetTransactionResponseDTO
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO
import com.tpay.sdk.server.dto.request.PayTransactionRequestDTO
import com.tpay.sdk.server.dto.response.CardTokenizationResponseDTO
import com.tpay.sdk.server.dto.response.CreateTransactionResponseDTO
import com.tpay.sdk.server.dto.response.GetChannelsResponseDTO
import com.tpay.sdk.server.dto.response.GetTransactionMethodsResponseDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Repository(private val serverService: ServerService) {
    // Save and restore to/from state handle
    internal var selectedPaymentMethod: PaymentMethod? = null
    internal var availablePaymentMethods: AvailablePaymentMethods? = null
        set(value) {
            field = value
            value?.run(::preloadImages)
        }
    internal var cardTokenTransaction: CardTokenTransaction? = null
    internal lateinit var transaction: Transaction
    internal lateinit var tokenization: Tokenization
    internal var transactionId: String? = null
    internal var tokenizationId: String? = null
    internal var webUrl: WebUrl? = null
    internal var internalRedirects = Redirects(
        successUrl = "https://secure.tpay.com/mobile-sdk/success",
        errorUrl = "https://secure.tpay.com/mobile-sdk/error"
    )

    // Saving to state handle not needed
    internal val preloadedImages: HashMap<String, BitmapDrawable> = hashMapOf()

    @Inject
    lateinit var cache: Cache

    init {
        injectFields()
    }

    private fun preloadImages(
        availablePaymentMethods: AvailablePaymentMethods
    ) = availablePaymentMethods.run {
        for (method in (availableTransfers + availablePekaoInstallmentMethods)) {
            if (preloadedImages[method.imageUrl] != null) continue
            getImageDrawable(method.imageUrl).observe(
                onSuccess = { image -> preloadedImages[method.imageUrl] = image },
                onError = {}
            )
        }
    }

    internal fun setAuth(
        authorization: Merchant.Authorization,
        environment: Environment
    ) {
        serverService.setAuth(authorization, environment)
    }

    internal fun tokenizeCard(
        cardTokenizationRequestDTO: CardTokenizationRequestDTO
    ): Completable<CardTokenizationResponseDTO> = serverService.tokenizeCard(cardTokenizationRequestDTO)

    internal fun getImageDrawable(imageUrl: String): Completable<BitmapDrawable> {
        return Completable.create { completable ->
            cache.getBankLogo(imageUrl).observe({ cachedImage ->
                completable.onSuccess(cachedImage.bytes.toBitmapDrawable())
            }, {
                serverService.getImage(imageUrl).observe({ bytes ->
                    if (bytes.isEmpty()) {
                        completable.onError(ImageEmptyException)
                    } else {
                        val imageToCache = CachedNetworkImage.from(imageUrl, bytes)
                        val bitmapDrawable = bytes.toBitmapDrawable()

                        if (imageToCache != null) {
                            cache.saveBankLogo(imageToCache).observe({
                                completable.onSuccess(bitmapDrawable)
                            }, {
                                completable.onSuccess(bitmapDrawable)
                            })
                        } else {
                            completable.onSuccess(bitmapDrawable)
                        }
                    }
                }, { e ->
                    completable.onError(e)
                })
            })
        }
    }

    internal fun getPaymentChannels(): Completable<GetChannelsResponseDTO> {
        return serverService.getPaymentChannels()
    }

    internal fun getAvailablePaymentMethods(): Completable<GetTransactionMethodsResponseDTO> {
        return serverService.getPaymentMethods()
    }

    internal fun createTransaction(createTransactionRequestDTO: CreateTransactionRequestDTO): Completable<CreateTransactionResponseDTO> {
        return serverService.createTransaction(createTransactionRequestDTO)
    }

    internal fun createTransaction(createTransactionWithChannelsDTO: CreateTransactionWithChannelsDTO): Completable<CreateTransactionResponseDTO> {
        return serverService.createTransaction(createTransactionWithChannelsDTO)
    }

    internal fun continueTransaction(
        transactionId: String,
        payTransactionRequestDTO: PayTransactionRequestDTO
    ): Completable<CreateTransactionResponseDTO> {
        return serverService.payForTransaction(transactionId, payTransactionRequestDTO)
    }

    internal fun getTransaction(transactionId: String): Completable<GetTransactionResponseDTO> {
        return serverService.getTransaction(transactionId)
    }
}