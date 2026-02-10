package com.tpay.sdk.server

import android.os.Build
import com.tpay.sdk.BuildConfig
import com.tpay.sdk.api.models.Compatibility
import java.net.HttpURLConnection
import javax.inject.Singleton

@Singleton
internal class RequestPropertyProvider() {
    private var sdkVersion = "tpay-android:${BuildConfig.VERSION_NAME}"

    fun setSdkVersion(compatibility: Compatibility, version: String?) {
        sdkVersion = when (compatibility) {
            Compatibility.NATIVE -> "tpay-android:${BuildConfig.VERSION_NAME}"
            Compatibility.FLUTTER -> "tpay-flutter:${version.orEmpty()}"
            Compatibility.REACT_NATIVE -> "tpay-react-native:${version.orEmpty()}"
        }
    }

    fun setRequestProperties(httpConnection: HttpURLConnection) {
        httpConnection.setRequestProperty("Content-Type", "application/json")
        httpConnection.setRequestProperty("Accept", "application/json")
        httpConnection.setRequestProperty("X-Client-Source", "tpay-com/$sdkVersion")
        httpConnection.setRequestProperty(
            "User-Agent",
            "$sdkVersion|${Build.MODEL}|${Build.VERSION.RELEASE}"
        )
    }
}