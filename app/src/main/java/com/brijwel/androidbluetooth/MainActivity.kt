package com.brijwel.androidbluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.brijwel.androidbluetooth.databinding.ActivityMainBinding
import com.brijwel.androidbluetooth.features.bluetoothdevices.BluetoothDevicesActivity
import com.brijwel.androidbluetooth.permissions.getRequiredBluetoothPermissions
import com.brijwel.androidbluetooth.permissions.hasAllPermission
import com.brijwel.androidbluetooth.utils.parcelable
import com.brijwel.androidbluetooth.utils.viewBinding

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityMainBinding.inflate(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.connect.setOnClickListener {
            onConnectToBluetoothDevice()
        }
    }

    private fun onConnectToBluetoothDevice() {
        if (hasAllPermission(getRequiredBluetoothPermissions())) {
            selectBluetoothDevice()
        } else {
            bluetoothPermissionLauncher.launch(getRequiredBluetoothPermissions())
        }
    }

    private fun selectBluetoothDevice() {
        selectBluetoothResult.launch(Intent(this, BluetoothDevicesActivity::class.java))
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (permission.all { it.value }) {
                selectBluetoothDevice()
            }
        }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private val selectBluetoothResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val bluetoothDevice = it
                    .data!!
                    .parcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                binding.selectedDevice.text =
                    """Selected Device
                        |Name : ${bluetoothDevice.name}
                        |Address : ${bluetoothDevice.address}
                    """.trimMargin()
            }
        }
}