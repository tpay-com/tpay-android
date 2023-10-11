package com.tpay.sdk.server

internal class HttpServerException(val code: Int, override val message: String?) : Exception()
internal class HttpClientException(val code: Int, override val message: String?) : Exception()
internal object NoInternetException : Exception()
internal class JsonParseException(override val message: String?) : Exception()
internal object ImageEmptyException : Exception()