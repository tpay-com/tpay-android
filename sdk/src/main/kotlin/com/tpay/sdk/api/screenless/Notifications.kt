package com.tpay.sdk.api.screenless

/**
 * Class responsible for storing information about notifications
 *
 * @param [notificationUrl] address of merchant server with configured endpoint
 * to receive notifications
 * @param [notificationEmail] merchant email address to receive payment notifications
 * */
data class Notifications(
    val notificationUrl: String,
    val notificationEmail: String
)
