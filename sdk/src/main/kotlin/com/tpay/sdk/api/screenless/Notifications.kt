package com.tpay.sdk.api.screenless

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing information about notifications
 *
 * @param [notificationUrl] address of merchant server with configured endpoint
 * to receive notifications
 * @param [notificationEmail] merchant email address to receive payment notifications
 * */
@Parcelize
data class Notifications(
    val notificationUrl: String,
    val notificationEmail: String
) : Parcelable
