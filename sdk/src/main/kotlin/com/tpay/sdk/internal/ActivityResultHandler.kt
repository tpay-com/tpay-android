package com.tpay.sdk.internal

import android.content.Intent
import com.tpay.sdk.extensions.Observable
import javax.inject.Singleton

@Singleton
internal class ActivityResultHandler {
    val onResult = Observable<Triple<Int, Int, Intent?>>()
}