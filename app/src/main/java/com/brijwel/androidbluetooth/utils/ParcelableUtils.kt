package com.brijwel.androidbluetooth.utils

import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable

@Suppress("unused")
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

@Suppress("unused")
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

@Suppress("unused")
inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

@Suppress("unused")
inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}