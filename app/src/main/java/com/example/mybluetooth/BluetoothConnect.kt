//package com.example.mybluetooth
//
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.util.Log
//import java.io.IOException
//import java.util.UUID
//
//private inner class ConnectThread(device: BluetoothDevice) : Thread() {
//
//    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
//        device.createRfcommSocketToServiceRecord(MY_UUID)
//    }
//
//    public override fun run() {
//        // Cancel discovery because it otherwise slows down the connection.
//        bluetoothAdapter?.cancelDiscovery()
//
//        mmSocket?.let { socket ->
//            // Connect to the remote device through the socket. This call blocks
//            // until it succeeds or throws an exception.
//            socket.connect()
//
//            // The connection attempt succeeded. Perform work associated with
//            // the connection in a separate thread.
//            manageMyConnectedSocket(socket)
//        }
//    }
//
//    // Closes the client socket and causes the thread to finish.
//    fun cancel() {
//        try {
//            mmSocket?.close()
//        } catch (e: IOException) {
//            Log.e(TAG, "Could not close the client socket", e)
//        }
//    }
//}
////class ConnectThread(private val device: BluetoothDevice, private val uuid: UUID) : Thread() {
////    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
////        device.createRfcommSocketToServiceRecord(uuid)
////    }
////
////    override fun run() {
////        // Cancel discovery because it otherwise slows down the connection.
////        bluetoothAdapter?.cancelDiscovery()
////
////        mmSocket?.use { socket ->
////            // Connect to the remote device through the socket. This call blocks
////            // until it succeeds or throws an exception.
////            socket.connect()
////
////            // The connection attempt succeeded. Perform work associated with
////            // the connection in a separate thread.
////            manageMyConnectedSocket(socket)
////        }
////    }
////
////    // Closes the client socket and causes the thread to finish.
////    fun cancel() {
////        try {
////            mmSocket?.close()
////        } catch (e: IOException) {
////            Log.e(TAG, "Could not close the client socket", e)
////        }
////    }
////}
////
////fun manageMyConnectedSocket(socket: BluetoothSocket) {
////    // Handle the Bluetooth connection in your own way
////}
////
////}
////
////
////
