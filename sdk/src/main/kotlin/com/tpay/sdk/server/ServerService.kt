package com.tpay.sdk.server

import com.tpay.sdk.api.models.Environment
import com.tpay.sdk.api.models.merchant.Merchant
import com.tpay.sdk.extensions.Completable
import com.tpay.sdk.extensions.Threads
import com.tpay.sdk.server.dto.request.CardTokenizationRequestDTO
import com.tpay.sdk.server.dto.request.CreateTransactionRequestDTO
import com.tpay.sdk.server.dto.request.CreateTransactionWithChannelsDTO
import com.tpay.sdk.server.dto.request.PayTransactionRequestDTO
import com.tpay.sdk.server.dto.response.*
import javax.inject.Singleton
import kotlin.Exception

@Singleton
internal class ServerService {
    private var networking: Networking = Networking(Environment.PRODUCTION.baseUrl)
    private var clientId: String = ""
    private var clientSecret: String = ""
    private var accessToken: AccessToken? = null

    fun setAuth(
        authorization: Merchant.Authorization,
        environment: Environment
    ){
        clientId = authorization.clientId
        clientSecret = authorization.clientSecret
        networking = Networking(environment.baseUrl)
        accessToken = null
    }

    private fun getToken(): Completable<String> {
        return networking.post(
            endpoint = "oauth/auth",
            auth = Auth.BasicAuth(clientId, clientSecret)
        )
    }

    fun tokenizeCard(
        cardTokenizationRequestDTO: CardTokenizationRequestDTO
    ) : Completable<CardTokenizationResponseDTO> = Completable.create { completable ->
        authorize({ accessToken ->
            networking.post(
                endpoint = "tokens",
                auth = Auth.BearerAuth(accessToken.token),
                body = cardTokenizationRequestDTO.toString()
            )
                .observeOn(Threads.IO)
                .observe({ response ->
                    try {
                        completable.onSuccess(CardTokenizationResponseDTO(response))
                    } catch (exception: Exception) {
                        completable.onError(JsonParseException(exception.message))
                    }
                }, { e ->
                    handleClientOrServerError(e)
                    completable.onError(e)
                })
        }, completable::onError)
    }
    
    fun getPaymentChannels(): Completable<GetChannelsResponseDTO> = Completable.create { completable ->
        authorize({ accessToken ->
            networking.get(
                endpoint = "transactions/channels",
                auth = Auth.BearerAuth(accessToken.token)
            )
                .observeOn(Threads.IO)
                .observe({ response ->
                    try {
                        completable.onSuccess(GetChannelsResponseDTO(response))
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        completable.onError(JsonParseException(exception.message))
                    }
                }, { e ->
                    handleClientOrServerError(e)
                    completable.onError(e)
                })
        }, completable::onError)
    }
    
    fun getPaymentMethods(): Completable<GetTransactionMethodsResponseDTO> = Completable.create { completable ->
        authorize({ accessToken ->
                networking.get(
                    endpoint = "transactions/bank-groups?onlyOnline=true",
                    auth = Auth.BearerAuth(accessToken.token)
                )
                    .observeOn(Threads.IO)
                    .observe({ response ->
                        try {
                            completable.onSuccess(GetTransactionMethodsResponseDTO(response))
                        } catch (exception: Exception) {
                            completable.onError(JsonParseException(exception.message))
                        }
                    }, { e ->
                        handleClientOrServerError(e)
                        completable.onError(e)
                    }
                )
            }, { e ->
                completable.onError(e)
            }
        )
    }

    fun getImage(imageUrl: String): Completable<ByteArray> {
        return Completable.create { completable ->
            networking.getImage(imageUrl, completable)
        }
    }
    
    fun createTransaction(createTransactionWithChannelsDTO: CreateTransactionWithChannelsDTO): Completable<CreateTransactionResponseDTO> {
        return Completable.create { completable ->
            authorize({ accessToken ->
                networking.post(
                    endpoint = "transactions",
                    auth = Auth.BearerAuth(accessToken.token),
                    body = createTransactionWithChannelsDTO.toString()
                )
                    .observeOn(Threads.IO)
                    .observe({ response ->
                        try {
                            completable.onSuccess(CreateTransactionResponseDTO(response))
                        } catch (exception: Exception) {
                            completable.onError(JsonParseException(exception.message))
                        }
                    }, { e ->
                        handleClientOrServerError(e)
                        completable.onError(e)
                    })
            }, completable::onError)
        }
    }

    fun createTransaction(createTransactionRequestDTO: CreateTransactionRequestDTO): Completable<CreateTransactionResponseDTO> {
        return Completable.create { completable ->
            authorize({ accessToken ->
                networking.post(
                    endpoint = "transactions",
                    auth = Auth.BearerAuth(accessToken.token),
                    body = createTransactionRequestDTO.toString()
                )
                    .observeOn(Threads.IO)
                    .observe({ response ->
                    try {
                        completable.onSuccess(CreateTransactionResponseDTO(response))
                    } catch (exception: Exception) {
                        completable.onError(JsonParseException(exception.message))
                    }
                }, { e ->
                    handleClientOrServerError(e)
                    completable.onError(e)
                })
            }, { e ->
                completable.onError(e)
            })
        }
    }

    fun payForTransaction(
        transactionId: String,
        payTransactionRequestDTO: PayTransactionRequestDTO
    ): Completable<CreateTransactionResponseDTO> {
        return Completable.create { completable ->
            authorize(
                onSuccess = { accessToken ->
                    networking.post(
                        endpoint = "transactions/$transactionId/pay",
                        auth = Auth.BearerAuth(accessToken.token),
                        body = payTransactionRequestDTO.toString()
                    )
                        .observeOn(Threads.IO)
                        .observe({ response ->
                            try {
                                completable.onSuccess(CreateTransactionResponseDTO(response))
                            } catch (exception: Exception) {
                                completable.onError(JsonParseException(exception.message))
                            }
                        }, { e ->
                            handleClientOrServerError(e)
                            completable.onError(e)
                        })
                },
                onFailure = { e ->
                    completable.onError(e)
                }
            )
        }
    }

    fun getTransaction(transactionId: String): Completable<GetTransactionResponseDTO> {
        return Completable.create { completable ->
            authorize(
                onSuccess = { accessToken ->
                    networking.get(
                        endpoint = "transactions/$transactionId",
                        auth = Auth.BearerAuth(accessToken.token)
                    )
                        .observeOn(Threads.IO)
                        .observe({ response ->
                            try {
                                completable.onSuccess(GetTransactionResponseDTO(response))
                            } catch (exception: Exception){
                                completable.onError(JsonParseException(exception.message))
                            }
                        }, { e ->
                            handleClientOrServerError(e)
                            completable.onError(e)
                        })
                },
                onFailure = { error ->
                    completable.onError(error)
                }
            )
        }
    }

    private fun handleClientOrServerError(exception: Exception) {
        if (exception is HttpClientException || exception is HttpServerException) {
            accessToken = null
        }
    }

    private fun authorize(onSuccess: (AccessToken) -> Unit, onFailure: (Exception) -> Unit) {
        val tempToken = accessToken
        if (tempToken == null || !tempToken.isValid) {
            getToken()
                .observeOn(Threads.IO)
                .observe({ response ->
                    try {
                        val tokenDTO = TokenResponseDTO(response)

                        val token = tokenDTO.accessToken
                        val expiresIn = tokenDTO.expiresIn

                        if (token == null || expiresIn == null) {
                            onFailure.invoke(IllegalStateException())
                            return@observe
                        }

                        AccessToken(token = token, validForSeconds = expiresIn).run {
                            accessToken = this
                            onSuccess.invoke(this)
                        }
                    } catch (exception: Exception) {
                        onFailure.invoke(JsonParseException(exception.message))
                    }
                }, { e ->
                    onFailure.invoke(e)
                })
        } else {
            onSuccess.invoke(tempToken)
        }
    }
}
