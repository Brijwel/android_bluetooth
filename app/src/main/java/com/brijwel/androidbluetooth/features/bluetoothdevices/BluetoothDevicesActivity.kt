package com.brijwel.androidbluetooth.features.bluetoothdevices

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.brijwel.androidbluetooth.databinding.ActivityBluetoothDevicesBinding
import com.brijwel.androidbluetooth.utils.parcelable
import com.brijwel.androidbluetooth.utils.viewBinding

@SuppressLint("MissingPermission")
class BluetoothDevicesActivity : AppCompatActivity() {

    companion object {
        const val SCAN_PERIOD = 15000L
    }

    private val binding by viewBinding { ActivityBluetoothDevicesBinding.inflate(it) }

    private var _bluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothAdapter: BluetoothAdapter get() = _bluetoothAdapter!!

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private var scanning = false
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val pairedDevices: MutableSet<BluetoothDeviceData> = mutableSetOf()
    private val nonPairedDevices: MutableSet<BluetoothDeviceData> = mutableSetOf()

    private val pairedDevicesAdapter = BluetoothDeviceAdapter {
        bluetoothDeviceSelected(it)
    }

    private val availableDevicesAdapter = BluetoothDeviceAdapter {
        bluetoothDeviceSelected(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.rvPairedDevices.adapter = pairedDevicesAdapter
        binding.rvAvailableDevices.adapter = availableDevicesAdapter
        registerBroadcastReceiver()
        initializeBluetoothAdapter()
        startDiscovery()

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }

    private fun initializeBluetoothAdapter() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        _bluetoothAdapter = bluetoothManager.adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        }
    }

    private fun fetchBluetoothDevices() {
        pairedDevices.addAll(bluetoothAdapter.bondedDevices.map {
            BluetoothDeviceData(
                it.name,
                it.address
            )
        })
        pairedDevicesAdapter.submitList(pairedDevices.toList())
        binding.noPairedDevices.isVisible = pairedDevices.isEmpty()
        binding.rvPairedDevices.isVisible = pairedDevices.isEmpty().not()
    }

    private fun startDiscovery() {
        if (bluetoothAdapter.isEnabled.not()) return
        //stop previous request
        stopDiscovery()
        fetchBluetoothDevices()
        //request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handler.postDelayed(
                endScanningRunnable,
                SCAN_PERIOD
            )
            bluetoothLeScanner!!.startScan(leScanCallback)
        }
        scanning = true
        binding.progressBar.isVisible = true
    }

    private fun stopDiscovery() {
        if (scanning && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handler.removeCallbacks(endScanningRunnable)
            bluetoothLeScanner!!.stopScan(leScanCallback)
        }
        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
        scanning = false
        binding.progressBar.isVisible = false
    }

    private fun registerBroadcastReceiver() {
        registerReceiver(broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(
            broadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        )
        registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
    }

    private fun unregisterBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver)
    }

    private fun bluetoothTurnedOff() {
        stopDiscovery()
    }

    private fun bluetoothTurnedOn() {
        startDiscovery()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result?.device != null) {
                updateBluetoothDevice(result.device)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val endScanningRunnable = Runnable {
        stopDiscovery()
    }


    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                -1
            )
            if (state == BluetoothAdapter.STATE_ON) {
                bluetoothTurnedOn()
                return
            } else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                bluetoothTurnedOff()
                return
            }
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.progressBar.isVisible = false
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val bluetoothDevice =
                        intent.parcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (bluetoothDevice != null) {
                        updateBluetoothDevice(bluetoothDevice)
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bluetoothDevice =
                        intent.parcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (bluetoothDevice != null) {
                        updateBluetoothDevice(bluetoothDevice)
                        if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED)
                            sendResultBack(bluetoothDevice)
                    }
                }
            }
        }

    }

    private fun updateBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        Log.d("BluetoothConnection", "Device $bluetoothDevice")
        val deviceData =
            BluetoothDeviceData(name = bluetoothDevice.name, address = bluetoothDevice.address)
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            pairedDevices.add(deviceData)
            nonPairedDevices.remove(deviceData)
        } else {
            nonPairedDevices.add(deviceData)
            pairedDevices.remove(deviceData)
        }

        pairedDevicesAdapter.submitList(pairedDevices.toList())
        availableDevicesAdapter.submitList(nonPairedDevices.toList())

        binding.noPairedDevices.isVisible = pairedDevices.isEmpty()
        binding.rvPairedDevices.isVisible = pairedDevices.isEmpty().not()
    }

    private fun bluetoothDeviceSelected(device: BluetoothDeviceData) {
        val bluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            Log.d("BluetoothConnection", "Device $bluetoothDevice \n Paired")
            sendResultBack(bluetoothDevice)
        } else {
            Log.d("BluetoothConnection", "Device $bluetoothDevice \n Un-Paired")
            if (bluetoothDevice.createBond().not())
                Toast.makeText(
                    this,
                    "Failed to pair with ${bluetoothDevice.name}",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun sendResultBack(device: BluetoothDevice? = null) {
        if (device != null) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(BluetoothDevice.EXTRA_DEVICE, device)
            })
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private val toggleBluetoothResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

        }
}