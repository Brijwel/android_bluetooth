package com.brijwel.androidbluetooth.features.bluetoothdevices

import java.util.*

data class BluetoothDeviceData(
    val name: String?,
    val address: String?,
    var status: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        return address == (other as BluetoothDeviceData?)?.address
    }

    override fun hashCode(): Int {
        return Objects.hash(address)
    }
}
