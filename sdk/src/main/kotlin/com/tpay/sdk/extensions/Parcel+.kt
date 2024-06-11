package com.tpay.sdk.extensions

import android.os.Parcel
import android.os.Parcelable

fun Parcel.readStringOrThrow(): String {
    return readString() ?: throw IllegalStateException("String null in parcel!")
}

inline fun <reified T : Parcelable> Parcel.readParcelableOrThrow(): T {
    return readParcelable(T::class.java.classLoader) ?: throw IllegalStateException("${T::class.java.simpleName} null in parcel!")
}