package com.tpay.sdk.api.screenless.googlePay

import com.google.android.gms.wallet.WalletConstants

/**
 * Enum defining environments for Google Pay
 */
enum class GooglePayEnvironment(val actual: Int) {
    PRODUCTION(WalletConstants.ENVIRONMENT_PRODUCTION),
    TEST(WalletConstants.ENVIRONMENT_TEST)
}