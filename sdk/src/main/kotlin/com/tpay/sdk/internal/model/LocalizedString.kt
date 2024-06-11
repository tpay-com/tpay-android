package com.tpay.sdk.internal.model

import android.os.Parcelable
import com.tpay.sdk.api.models.Language
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class LocalizedString(
    val language: Language,
    val value: String
) : Parcelable
