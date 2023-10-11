package com.tpay.sdk.server

import android.util.Base64
import android.util.Log
import com.tpay.sdk.extensions.*
import com.tpay.sdk.server.dto.ErrorResponseDTO
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Executors

internal class Networking(baseUrl: String) {
    private var baseUrl = baseUrl.addAtEndIfNotThere("/")
        set(value) {
            field = value.addAtEndIfNotThere("/")
        }
    private val ioExecutor = Executors.newCachedThreadPool()

    fun get(endpoint: String, auth: Auth? = null): Completable<String> {
        return Completable.create { completable ->
            makeRequest(method = "GET", endpoint = endpoint, completable = completable, auth = auth)
        }
    }

    fun post(endpoint: String, auth: Auth? = null, body: String? = null): Completable<String> {
        return Completable.create { completable ->
            makeRequest(method = "POST", endpoint = endpoint, auth = auth, body =  body, completable = completable)
        }
    }

    fun getImage(
        imageUrl: String,
        completable: Completable<ByteArray>
    ) {
        ioExecutor.execute {
            try {
                val httpConnection = URL(imageUrl).openConnection() as HttpURLConnection
                val bytes = httpConnection.inputStream.readBytes()

                httpConnection.responseCode.let { code ->
                    when {
                        isServerError(code) -> {
                            val errorResponse = ErrorResponseDTO(readErrorMessage(httpConnection))
                            completable.onError(HttpServerException(code, errorResponse.readErrorMessages()))
                        }
                        isClientError(code) -> {
                            val errorResponse = ErrorResponseDTO(readErrorMessage(httpConnection))
                            completable.onError(HttpClientException(code, errorResponse.readErrorMessages()))
                        }
                        else -> {
                            completable.onSuccess(bytes)
                        }
                    }
                }
                httpConnection.disconnect()
            } catch (exception: Exception){
                completable.onError(NoInternetException)
            } catch (exception: JSONException){
                completable.onError(JsonParseException(exception.message))
            }
        }
    }

    private fun makeRequest(
        method: String,
        endpoint: String,
        completable: Completable<String>,
        auth: Auth? = null,
        body: String? = null
    ) {
        ioExecutor.execute {
            try {
                val url = URL("$baseUrl$endpoint")
                val httpConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpConnection.connectTimeout = 60000
                httpConnection.requestMethod = method

                httpConnection.setRequestProperty("Content-Type", "application/json")
                httpConnection.setRequestProperty("Accept", "application/json")

                auth?.let {
                    httpConnection.setRequestProperty(it.header, it.value)
                }

                logRequest(httpConnection, method, body)
                val timestamp = System.currentTimeMillis()

                if (body != null) {
                    httpConnection.doOutput = true
                    val bodyBytes: ByteArray = body.toByteArray(Charset.forName(("UTF-8")))
                    httpConnection.outputStream.write(bodyBytes, 0, bodyBytes.size)
                }

                httpConnection.responseCode.let { code ->
                    when {
                        isServerError(code) -> {
                            val errorResponse = ErrorResponseDTO(readErrorMessage(httpConnection))
                            completable.onError(HttpServerException(code, errorResponse.readErrorMessages()))
                        }
                        isClientError(code) -> {
                            val errorResponse = ErrorResponseDTO(readErrorMessage(httpConnection))
                            completable.onError(HttpClientException(code, errorResponse.readErrorMessages()))
                        }
                        else -> {
                            val responseMessage = readResponseMessage(httpConnection)
                            logResponse(
                                httpConnection,
                                responseMessage,
                                responseTime = (System.currentTimeMillis() - timestamp)
                            )
                            completable.onSuccess(responseMessage)
                        }
                    }
                }
                httpConnection.disconnect()
            } catch (exception: Exception) {
                completable.onError(NoInternetException)
            } catch (exception: JSONException){
                completable.onError(JsonParseException(exception.message))
            }
        }
    }

    private fun logRequest(httpConnection: HttpURLConnection, method: String, body: String?) {
        if (isloggingEnabled) {
            Log.d(TAG, "--> $method ${httpConnection.url}")
            httpConnection.requestProperties.forEach {
                Log.d(TAG, "${it.key} ${it.value.joinToString(",")}")
            }
            body?.let {
                Log.d(TAG, it)
            }
            Log.d(TAG, "--> END $method")
        }
    }

    private fun readErrorMessage(httpConnection: HttpURLConnection): String {
        return readFromStream(httpConnection.errorStream)
    }

    private fun readResponseMessage(httpConnection: HttpURLConnection): String {
        return readFromStream(httpConnection.inputStream)
    }

    private fun readFromStream(stream: InputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(stream))

        var line: String? = bufferedReader.readLine()
        val response = StringBuilder()
        while (line != null) {
            response.append(line)
            line = bufferedReader.readLine()
        }

        return response.toString()
    }

    private fun logResponse(
        httpConnection: HttpURLConnection,
        responseMessage: String,
        responseTime: Long
    ) {
        if (isloggingEnabled) {
            Log.d(
                TAG,
                "<-- ${httpConnection.responseCode} ${httpConnection.responseMessage} ${httpConnection.url} ($responseTime ms)"
            )
            httpConnection.headerFields.forEach {
                if (it.key != null && !it.key.startsWith("X-Android")) {
                    Log.d(TAG, "${it.key} ${it.value.joinToString(",")}")
                }
            }
            Log.d(TAG, responseMessage)
        }
    }

    private fun isServerError(code: Int): Boolean {
        return code in 500..599
    }

    private fun isClientError(code: Int): Boolean {
        return code in 400..499
    }

    companion object {
        private const val TAG = "HTTP"
    }
}

internal sealed class Auth(val header: String = "Authorization", var value: String = "") {
    internal data class BasicAuth(val username: String, val password: String) : Auth() {
        init {
            value =
                "Basic " + Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    Base64.NO_WRAP
                )
        }
    }

    internal data class BearerAuth(val token: String) : Auth() {
        init {
            value = "Bearer $token"
        }
    }
}