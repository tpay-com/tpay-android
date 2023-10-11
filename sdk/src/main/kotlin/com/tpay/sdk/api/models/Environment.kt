package com.tpay.sdk.api.models

enum class Environment(internal val baseUrl: String) {
    PRODUCTION("https://api.tpay.com"),
    SANDBOX("https://openapi.sandbox.tpay.com")
}