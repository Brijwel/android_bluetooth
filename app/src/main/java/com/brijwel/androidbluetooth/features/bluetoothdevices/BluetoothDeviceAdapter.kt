package com.brijwel.androidbluetooth.features.bluetoothdevices

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brijwel.androidbluetooth.databinding.ItemBluetoothDeviceBinding

class BluetoothDeviceAdapter(
    private val onDeviceSelected: (BluetoothDeviceData) -> Unit
) :
    ListAdapter<BluetoothDeviceData, BluetoothDeviceAdapter.BluetoothDeviceViewHolder>(diffUtil) {

    inner class BluetoothDeviceViewHolder
        (val binding: ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    getItem(adapterPosition)?.let {
                        onDeviceSelected(it)
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder(
            ItemBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        getItem(position)?.let {
            holder.binding.device.text = if (it.name.isNullOrEmpty().not()) {
                "${it.name}\n${it.address}"
            } else {
                it.address
            }
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<BluetoothDeviceData>() {
            override fun areItemsTheSame(
                oldItem: BluetoothDeviceData,
                newItem: BluetoothDeviceData
            ): Boolean {
                return oldItem.address == newItem.address
            }

            override fun areContentsTheSame(
                oldItem: BluetoothDeviceData,
                newItem: BluetoothDeviceData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}