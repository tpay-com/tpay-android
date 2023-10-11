package com.tpay.sdk.api.models

import android.content.Intent

interface ActivityResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}