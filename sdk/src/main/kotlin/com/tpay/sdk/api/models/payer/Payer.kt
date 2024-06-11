package com.tpay.sdk.api.models.payer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class responsible for storing payer information.
 */
@Parcelize
data class Payer(
    var name: String,
    var email: String,
    var phone: String?,
    var address: Address?
) : Parcelable {
    @Parcelize
    data class Address(
        var address: String?,
        var city: String?,
        var countryCode: String?,
        var postalCode: String?
    ): Parcelable

    internal fun setAddress(value: String) {
        address?.run {
            address = value
        } ?: kotlin.run {
            address = Address(
                address = value,
                city = null,
                countryCode = null,
                postalCode = null
            )
        }
    }

    internal fun setPostalCode(value: String) {
        address?.run {
            postalCode = value
        } ?: kotlin.run {
            address = Address(
                address = null,
                city = null,
                countryCode = null,
                postalCode = value
            )
        }
    }

    internal fun setCity(value: String) {
        address?.run {
            city = value
        } ?: kotlin.run {
            address = Address(
                address = null,
                city = value,
                countryCode = null,
                postalCode = null
            )
        }
    }
}
