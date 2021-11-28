package xyz.ridsoft.doorlock

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

class BluetoothUtils {
    companion object {
        fun findBLECharacteristics(gatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
            val result = arrayListOf<BluetoothGattCharacteristic>()
            val services = gatt.services
            val service = findGattService(services) ?: return result
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (isMatchingCharacteristic(characteristic)) {
                    result.add(characteristic)
                }
            }
            return result
        }

        private fun findGattService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
            for (service in serviceList) {
                val serviceUuidString = service.uuid.toString()
                if (matchServiceUUIDString(serviceUuidString)) {
                    return service
                }
            }
            return null
        }

        fun findCommandCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, MainActivity.CHARACTERISTIC_COMMAND_STRING)
        }

        fun findResponseCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, MainActivity.CHARACTERISTIC_RESPONSE_STRING)
        }

        private fun findCharacteristic(
            gatt: BluetoothGatt,
            uuidString: String
        ): BluetoothGattCharacteristic? {
            val serviceList = gatt.services
            val service = findGattService(serviceList) ?: return null
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (matchCharacteristic(characteristic, uuidString)) {
                    return characteristic
                }
            }
            return null
        }

        private fun matchCharacteristic(
            characteristic: BluetoothGattCharacteristic?,
            uuidString: String
        ): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchUUIDs(uuid.toString(), uuidString)
        }

        private fun matchServiceUUIDString(serviceUuidString: String): Boolean {
            return matchUUIDs(serviceUuidString, MainActivity.UUID_SERVICE)
        }

        private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchCharacteristicUUID(uuid.toString())
        }

        private fun matchCharacteristicUUID(characteristicUuid: String): Boolean {
            return matchUUIDs(
                characteristicUuid,
                MainActivity.CHARACTERISTIC_COMMAND_STRING,
                MainActivity.CHARACTERISTIC_RESPONSE_STRING
            )
        }

        private fun matchUUIDs(uuidString: String, vararg matches: String): Boolean {
            for (match in matches) {
                if (uuidString.equals(match, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}