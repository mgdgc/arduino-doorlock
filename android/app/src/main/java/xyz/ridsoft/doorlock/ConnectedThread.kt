package xyz.ridsoft.doorlock

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ConnectedThread(private val socket: BluetoothSocket): Thread() {

    private var inputStream: InputStream = socket.inputStream
    private var outputStream: OutputStream = socket.outputStream

    override fun run() {
        var buffer: ByteArray
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream.available()

                if (bytes != 0) {
                    buffer = ByteArray(1024)
                    Thread.sleep(100)
                    bytes = inputStream.available()
                    bytes = inputStream.read(buffer, 0, bytes)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                break
            }
        }
    }

    fun write(message: String) {
        val bytes = message.toByteArray()
        try {
            outputStream.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            socket.close()
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}