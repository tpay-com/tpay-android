package com.tpay.sdk.api.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing information about Google Pay configuration
 *
 * @param [merchantId] your merchant id in Tpay system
 */
@Parcelize
data class GooglePayConfiguration(val merchantId: String) : Parcelable
