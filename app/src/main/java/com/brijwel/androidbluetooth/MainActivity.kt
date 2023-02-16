package com.brijwel.androidbluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.brijwel.androidbluetooth.databinding.ActivityMainBinding
import com.brijwel.androidbluetooth.features.bluetoothdevices.BluetoothDevicesActivity
import com.brijwel.androidbluetooth.permissions.getRequiredBluetoothPermissions
import com.brijwel.androidbluetooth.permissions.hasAllPermission
import com.brijwel.androidbluetooth.utils.parcelable
import com.brijwel.androidbluetooth.utils.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (hasAllPermission(getRequiredBluetoothPermissions())) {
                selectBluetoothDevice()
            } else {
                bluetoothPermissionLauncher.launch(getRequiredBluetoothPermissions())
            }
        } else {
            selectBluetoothDevice()
        }
    }


    private fun selectBluetoothDevice() {
        selectBluetoothResult.launch(Intent(this, BluetoothDevicesActivity::class.java))
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (permission.all { it.value }) {
                selectBluetoothDevice()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.nearby_devices_permission_message)
                    .setPositiveButton(getString(R.string.app_settings)) { _: DialogInterface, _: Int ->
                        settingsLauncher.launch(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                            )
                        )
                    }
                    .show()
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

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (hasAllPermission(getRequiredBluetoothPermissions()))
                    selectBluetoothDevice()
                else MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.requires_nearby_devices_permission_to_connect_with_printer)
                    .setPositiveButton(R.string.ok_caps, null)
                    .show()
            }
        }
}