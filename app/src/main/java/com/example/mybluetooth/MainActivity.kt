package com.example.mybluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {

//    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
//        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothManager.adapter
//    }
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    // ArrayAdapter to manage the display of discovered devices in the ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var buttonDiscover: Button
    private var pairedDevices: Set<BluetoothDevice>? = null
    private var bluetoothDevices: MutableList<BluetoothDevice> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        buttonDiscover = findViewById(R.id.button_Discover)
        // Setup the ListView and ArrayAdapter to display discovered devices
        val listView: ListView = findViewById(R.id.listViewScannedDevices)
//        arrayAdapterDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
//        listView.adapter = arrayAdapterDevices
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = arrayAdapter

        // IntentFilter to listen for Bluetooth devices found during discovery
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        val pairingFilter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, pairingFilter)

        buttonDiscover.setOnClickListener {
            startDiscovery()
        }

        listView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
//            val device = bluetoothAdapter?.getRemoteDevice("Device_Address")
//            val socket = device?.createRfcommSocketToServiceRecord(MY_UUID)
//
//            socket.connect()

            val deviceSelected = bluetoothDevices[position]
            Log.d( "Selected Cell", selectedItem)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                // Cancel discovery because it otherwise slows down the connection.
//                bluetoothAdapter?.cancelDiscovery()
            }else{
                // Always cancel discovery before connecting since it slows down the connection
//                bluetoothAdapter?.cancelDiscovery()

                val deviceMac = deviceSelected.address
                Log.d( "Selected Cell", deviceMac)
                val device = bluetoothAdapter?.getRemoteDevice(deviceMac)

                val socket = device?.createRfcommSocketToServiceRecord(MY_UUID)
                if (device != null) {
                    device.createBond()
                }
                try {
                    socket?.connect() // This might throw an IOException
                    onConnectionSuccess()
                } catch (e: IOException) {
                    // Handle connection failure
                    if (socket != null) {
                        if (socket.isConnected) {
                            // Safe to read data
                            try {
                                val inputStream = socket.inputStream
                                val buffer = ByteArray(1024)  // Adjust size based on expected data
                                val bytesRead = inputStream.read(buffer)
                                // Handle the read data
                            } catch (e: IOException) {
                                Log.e("Bluetooth", "Error reading from Bluetooth socket", e)
                            }
                        } else {
                            Log.e("Bluetooth", "Socket is closed or not connected")
                        }
                    }
                    e.printStackTrace()
                    try {
                        socket?.close()
                    } catch (e2: IOException) {
                        e2.printStackTrace()
                    }
                    return@setOnItemClickListener
                }
            }
        }
    }

    private fun onConnectionSuccess() {
        // Run on the UI thread
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Connected successfully", Toast.LENGTH_SHORT).show()
            // Additional UI updates or actions can be triggered here
        }
    }
    private val pairingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action//intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    // Check for BLUETOOTH_CONNECT permission before accessing device name and address
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                        val bondState = device.bondState
                        val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                        when (bondState) {
                            BluetoothDevice.BOND_BONDED -> {
                                // Successfully paired
                                Log.d("Blutooth","Connected")
                            }
                            BluetoothDevice.BOND_BONDING -> {
                                // Pairing in progress
                                Log.d("Blutooth","Pairing")

                            }
                            BluetoothDevice.BOND_NONE -> {
                                // Pairing broken or failed
                                Log.d("Blutooth","Pairing broken")

                            }
                        }
                    }
                }
            }
        }
    }

    // Method to start the Bluetooth discovery process
    private fun startDiscovery() {
        //if S then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.i("pairedDevices","$deviceName:$deviceHardwareAddress")
                }
                bluetoothAdapter?.startDiscovery()
            } else {
                // Show a toast message if BLUETOOTH_SCAN permission is not granted
                Toast.makeText(this, "BLUETOOTH_SCAN permission not granted", Toast.LENGTH_SHORT).show()

            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.i("pairedDevices","$deviceName:$deviceHardwareAddress")
                }
                bluetoothAdapter?.startDiscovery()
            } else {
                // Show a toast message if BLUETOOTH_SCAN permission is not granted
                Toast.makeText(this, "BLUETOOTH_SCAN permission not granted", Toast.LENGTH_SHORT).show()

            }
        }


    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { !it.value }) {
            Toast.makeText(this, "Required permissions needed", Toast.LENGTH_LONG).show()
            finish()
        } else {
            recreate()
        }
        Log.d("Permission",permissions.toString())
    }

    private val requestBLPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { !it.value }) {
            Toast.makeText(this, "Required permissions needed", Toast.LENGTH_LONG).show()
            finish()
        } else {
            recreate()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Retrieve the BluetoothDevice from the intent
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            // Check for BLUETOOTH_CONNECT permission before accessing device name and address
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Add the device name and address to the ArrayAdapter for display in the ListView
                                Toast.makeText(
                                    context,
                                    "Found device: ${device.name}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                arrayAdapter.add("${it.name ?: "Unknown"} | ${it.address}")
                                bluetoothDevices.add(device)
                            }
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Retrieve the BluetoothDevice from the intent
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            // Check for BLUETOOTH_CONNECT permission before accessing device name and address
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH
                                )
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Add the device name and address to the ArrayAdapter for display in the ListView
                                Toast.makeText(
                                    context,
                                    "Found device: ${device.name}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                arrayAdapter.add("${it.name ?: "Unknown"} | ${it.address}")
                                bluetoothDevices.add(device)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestMultiplePermissions.launch(
                REQUIRED_PERMISSIONS
            )
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.isEmpty() || permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

    }

    // Example of a universally unique identifier (UUID) for this application's Bluetooth service
    private val MY_UUID = UUID.randomUUID()

}

