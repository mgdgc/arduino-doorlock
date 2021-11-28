package xyz.ridsoft.doorlock

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import xyz.ridsoft.doorlock.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSION = 1001
        const val UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
        const val UUID_TX = "0000ffe1-0000-1000-8000-00805f9b34fb"
        const val UUID_RX = "0000ffe1-0000-1000-8000-00805f9b34fb"

        const val SERVICE_STRING = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
//        const val CHARACTERISTIC_COMMAND_STRING = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
//        const val CHARACTERISTIC_RESPONSE_STRING = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

        const val CHARACTERISTIC_COMMAND_STRING = "0000ffe1-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_RESPONSE_STRING = "0000ffe1-0000-1000-8000-00805f9b34fb"

        //BluetoothGattDescriptor 고정
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }

    private lateinit var binding: ActivityMainBinding

    private var btAdapter: BluetoothAdapter? = null
    private var btDevices: ArrayList<BluetoothDevice> = arrayListOf()

    private var btServerSocket: BluetoothServerSocket? = null
    private var connectedThread: ConnectedThread? = null
    private var btGatt: BluetoothGatt? = null

    private var scanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMain)

        requestPermissions()
        requestBluetooth()

        // Lock button
        binding.cardMainLock.setOnClickListener {
            write("")
        }

        // Unlock button
        binding.cardMainUnlock.setOnClickListener {
        }
    }

    private fun requestPermissions() {
        val permissions = arrayListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(0, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        var permissionsGranted = true

        for (p in permissions) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, p)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsGranted = false
                break
            }
        }

        if (permissionsGranted) {
            val result =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    var allGranted = true
                    for (granted in it.values) {
                        if (!granted) {
                            allGranted = false
                            break
                        }
                    }

                    if (allGranted) {

                    }
                }
            result.launch(permissions.toTypedArray())
        }
    }

    private fun requestBluetooth() {
        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        if (btAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val result =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {

                    }
                }
            result.launch(intent)
        }
    }

    private fun scanLeDevices() {
        if (btAdapter == null) return

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result != null) {
                    btDevices.add(result.device)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                if (results != null) {
                    for (result in results) {
                        btDevices.add(result.device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e("BLE Scan", "error code $errorCode")
            }
        }

        if (scanning) {
            scanning = false
            btAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
            scanResult()
        } else {
            btDevices.clear()

            val filters: MutableList<ScanFilter> = ArrayList()
            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(UUID_SERVICE)))
                .build()
            filters.add(scanFilter)

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            btAdapter!!.bluetoothLeScanner.startScan(null, settings, scanCallback)
            scanning = true
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (scanning) {
                btAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
                scanResult()
            }
            scanning = false
        }, 5000)
    }

    private fun scanResult() {
        if (btDevices.isEmpty()) {
            Snackbar.make(binding.layoutMain, "No devices", Snackbar.LENGTH_LONG)
                .setAction(R.string.confirm) { }
                .show()
            return
        }

        val btArrayAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice)
        for (i in btDevices.indices) {
            btArrayAdapter.add("$i: ${btDevices[i].name}")
        }

        val dialog = AlertDialog.Builder(this@MainActivity)
        dialog.setTitle(R.string.bluetooth_connect)
            .setAdapter(btArrayAdapter) { d, position ->
                btGatt = btDevices[position].connectGatt(
                    this@MainActivity,
                    false,
                    object : BluetoothGattCallback() {
                        override fun onConnectionStateChange(
                            gatt: BluetoothGatt?,
                            status: Int,
                            newState: Int
                        ) {
                            super.onConnectionStateChange(gatt, status, newState)
                            if (status != BluetoothGatt.GATT_SUCCESS) {
                                disconnectGattServer()
                                return
                            }

                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                gatt?.discoverServices()
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                disconnectGattServer()
                            }
                        }

                        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                            super.onServicesDiscovered(gatt, status)
                        }

                        override fun onCharacteristicChanged(
                            gatt: BluetoothGatt?,
                            characteristic: BluetoothGattCharacteristic?
                        ) {
                            super.onCharacteristicChanged(gatt, characteristic)
                            if (characteristic != null) {
                                readCharacteristic(characteristic)
                            }
                        }

                        override fun onCharacteristicRead(
                            gatt: BluetoothGatt?,
                            characteristic: BluetoothGattCharacteristic?,
                            status: Int
                        ) {
                            super.onCharacteristicRead(gatt, characteristic, status)
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                if (characteristic != null) {
                                    readCharacteristic(characteristic)
                                }
                            } else {
                                disconnectGattServer()
                            }
                        }

                        override fun onCharacteristicWrite(
                            gatt: BluetoothGatt?,
                            characteristic: BluetoothGattCharacteristic?,
                            status: Int
                        ) {
                            super.onCharacteristicWrite(gatt, characteristic, status)
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                disconnectGattServer()
                            }
                        }

                    })

                d.dismiss()
            }
            .setPositiveButton(R.string.close) { d, _ -> d.dismiss() }
            .show()
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {

    }

    private fun disconnectGattServer() {
        if (btGatt != null) {
            btGatt!!.disconnect()
            btGatt!!.close()
        }
    }

    private fun write(message: String) {
        val cmdCharacteristic = BluetoothUtils.findCommandCharacteristic(btGatt!!)
        // disconnect if the characteristic is not found
        if (cmdCharacteristic == null) {
            Log.e("write", "no characteristic")
            disconnectGattServer()
            return
        }
        val cmdBytes = message.toByteArray()
        cmdCharacteristic.value = cmdBytes
        val success: Boolean = btGatt!!.writeCharacteristic(cmdCharacteristic)
        if (!success) {
            Log.e("write", "Failed")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_bt) {
            scanLeDevices()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}