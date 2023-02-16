package com.brijwel.androidbluetooth.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


fun Activity.hasAllPermission(permissions: Array<String>): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasAllPermission(permissions: Array<String>): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Fragment.hasAllPermission(permissions: Array<String>): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun getRequiredBluetoothPermissions(): Array<String> {
    return arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
}

/**
 * Location permission needed only if your app derives physical location from Bluetooth scan results.
 */
fun getRequiredBluetoothPermissionsForLocation(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

private fun Context.canConnectBluetoothDevice() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else true

private fun Context.canScanBluetoothDevice() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH_SCAN
    ) == PackageManager.PERMISSION_GRANTED
} else true