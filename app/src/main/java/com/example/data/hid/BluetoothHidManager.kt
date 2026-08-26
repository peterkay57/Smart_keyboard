package com.example.data.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.data.model.ConnectedDeviceInfo
import com.example.data.model.HidConnectionState
import com.example.data.model.KeystrokeLogItem
import com.example.data.model.LedStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class BluetoothHidManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothHidManager"
        private const val SDP_NAME = "Bluetooth Keyboard"
        private const val SDP_DESCRIPTION = "Android Bluetooth HID Keyboard & Peripheral"
        private const val SDP_PROVIDER = "Android"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val executor = Executors.newSingleThreadExecutor()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null

    private val _connectionState = MutableStateFlow(HidConnectionState.NOT_INITIALIZED)
    val connectionState: StateFlow<HidConnectionState> = _connectionState.asStateFlow()

    private val _isAppRegistered = MutableStateFlow(false)
    val isAppRegistered: StateFlow<Boolean> = _isAppRegistered.asStateFlow()

    private val _connectedDevice = MutableStateFlow<ConnectedDeviceInfo?>(null)
    val connectedDevice: StateFlow<ConnectedDeviceInfo?> = _connectedDevice.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<ConnectedDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<ConnectedDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _ledStatus = MutableStateFlow(LedStatus())
    val ledStatus: StateFlow<LedStatus> = _ledStatus.asStateFlow()

    private val _keystrokeLogs = MutableStateFlow<List<KeystrokeLogItem>>(emptyList())
    val keystrokeLogs: StateFlow<List<KeystrokeLogItem>> = _keystrokeLogs.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    private val _isHidSupported = MutableStateFlow(true)
    val isHidSupported: StateFlow<Boolean> = _isHidSupported.asStateFlow()

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "BluetoothProfile.HID_DEVICE proxy connected")
                hidDevice = proxy as? BluetoothHidDevice
                _isHidSupported.value = true
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "BluetoothProfile.HID_DEVICE proxy disconnected")
                hidDevice = null
                _isAppRegistered.value = false
                _connectionState.value = HidConnectionState.NOT_INITIALIZED
                _connectedDevice.value = null
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, pluggedDevice=$pluggedDevice")
            _isAppRegistered.value = registered
            if (registered) {
                if (_connectionState.value == HidConnectionState.NOT_INITIALIZED ||
                    _connectionState.value == HidConnectionState.NO_PERMISSIONS
                ) {
                    _connectionState.value = HidConnectionState.READY_UNPAIRED
                }
            } else {
                _connectionState.value = HidConnectionState.NOT_INITIALIZED
            }
            refreshPairedDevices()
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            val devInfo = getDeviceInfo(device)
            Log.d(TAG, "onConnectionStateChanged: device=${devInfo.name}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = HidConnectionState.CONNECTED
                    _connectedDevice.value = devInfo
                    logKeystroke("Connected to ${devInfo.name}", "[STATE: CONNECTED]")
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = HidConnectionState.CONNECTING
                    _connectedDevice.value = devInfo
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    _connectionState.value = HidConnectionState.DISCONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = HidConnectionState.READY_UNPAIRED
                    _connectedDevice.value = null
                    logKeystroke("Disconnected from ${devInfo.name}", "[STATE: DISCONNECTED]")
                }
            }
            refreshPairedDevices()
        }

        override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport: type=$type, id=$id")
            val emptyData = ByteArray(bufferSize.coerceAtLeast(8))
            hidDevice?.replyReport(device, type, id, emptyData)
        }

        override fun onSetReport(device: BluetoothDevice, type: Byte, id: Byte, data: ByteArray) {
            Log.d(TAG, "onSetReport: type=$type, id=$id, size=${data.size}")
            if (data.isNotEmpty()) {
                val ledByte = data[0].toInt()
                val numLock = (ledByte and 0x01) != 0
                val capsLock = (ledByte and 0x02) != 0
                val scrollLock = (ledByte and 0x04) != 0
                _ledStatus.value = LedStatus(numLock, capsLock, scrollLock)
                logKeystroke("Host LED Update", "NumLock=$numLock, CapsLock=$capsLock, ScrollLock=$scrollLock")
            }
            hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        }

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
            Log.d(TAG, "onSetProtocol: protocol=$protocol")
        }

        override fun onInterruptData(device: BluetoothDevice, reportId: Byte, data: ByteArray) {
            Log.d(TAG, "onInterruptData: reportId=$reportId")
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            Log.d(TAG, "onVirtualCableUnplug: device=${device.address}")
            _connectionState.value = HidConnectionState.READY_UNPAIRED
            _connectedDevice.value = null
        }
    }

    @SuppressLint("MissingPermission")
    fun initialize() {
        if (bluetoothAdapter == null) {
            _connectionState.value = HidConnectionState.UNSUPPORTED
            _lastErrorMessage.value = "Bluetooth is not supported on this device"
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = HidConnectionState.BLUETOOTH_DISABLED
            return
        }

        try {
            val success = bluetoothAdapter.getProfileProxy(
                context,
                profileListener,
                BluetoothProfile.HID_DEVICE
            )
            if (!success) {
                _isHidSupported.value = false
                _connectionState.value = HidConnectionState.UNSUPPORTED
                _lastErrorMessage.value = "Bluetooth HID Device profile is not supported by Android OS/Hardware on this device."
            }
        } catch (e: SecurityException) {
            _connectionState.value = HidConnectionState.NO_PERMISSIONS
            _lastErrorMessage.value = "Bluetooth permissions missing: ${e.localizedMessage}"
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing HID proxy", e)
            _lastErrorMessage.value = "Failed to initialize HID proxy: ${e.message}"
        }
    }

    @SuppressLint("MissingPermission")
    fun registerApp() {
        val hid = hidDevice ?: return
        try {
            // Subclass byte: 0xC0 (Combo device: keyboard + mouse) or 0x40 (Keyboard)
            val subclassCombo: Byte = 0xC0.toByte()
            val sdp = BluetoothHidDeviceAppSdpSettings(
                SDP_NAME,
                SDP_DESCRIPTION,
                SDP_PROVIDER,
                subclassCombo,
                HidConstants.HID_REPORT_DESCRIPTOR
            )

            val registered = hid.registerApp(
                sdp,
                null,
                null,
                executor,
                hidCallback
            )
            Log.d(TAG, "registerApp returned $registered")
            if (!registered) {
                _lastErrorMessage.value = "Could not register HID App. Try toggling Bluetooth."
            }
        } catch (e: SecurityException) {
            _connectionState.value = HidConnectionState.NO_PERMISSIONS
            _lastErrorMessage.value = "Permission error: ${e.localizedMessage}"
        } catch (e: Exception) {
            Log.e(TAG, "Error registering HID app", e)
            _lastErrorMessage.value = "Registration error: ${e.message}"
        }
    }

    @SuppressLint("MissingPermission")
    fun unregisterApp() {
        try {
            hidDevice?.unregisterApp()
            _isAppRegistered.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering HID app", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        try {
            val bonded = adapter.bondedDevices
            val list = bonded?.map { getDeviceInfo(it) } ?: emptyList()
            _pairedDevices.value = list
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied while getting bonded devices")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        val hid = hidDevice ?: return
        try {
            _connectionState.value = HidConnectionState.CONNECTING
            _connectedDevice.value = getDeviceInfo(device)
            hid.connect(device)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to device", e)
            _lastErrorMessage.value = "Failed to connect: ${e.message}"
            _connectionState.value = HidConnectionState.READY_UNPAIRED
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice(device: BluetoothDevice? = null) {
        val hid = hidDevice ?: return
        val target = device ?: _connectedDevice.value?.bluetoothDevice
        if (target != null) {
            try {
                _connectionState.value = HidConnectionState.DISCONNECTING
                hid.disconnect(target)
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting device", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendKeyPress(keyCode: Byte, modifiers: Byte = HidConstants.MOD_NONE, desc: String = "Key 0x%02X".format(keyCode)) {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val pressReport = byteArrayOf(modifiers, 0x00, keyCode, 0x00, 0x00, 0x00, 0x00, 0x00)
        val releaseReport = byteArrayOf(modifiers, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        val hexReport = "[ID:1 mod:0x%02X key:0x%02X]".format(modifiers, keyCode)
        logKeystroke(desc, hexReport)

        if (targetDevice != null && hidDevice != null) {
            try {
                hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), pressReport)
                scope.launch {
                    delay(15)
                    hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), releaseReport)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending key press", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendKeyDown(keyCode: Byte, modifiers: Byte = HidConstants.MOD_NONE, desc: String = "KeyDown 0x%02X".format(keyCode)) {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val pressReport = byteArrayOf(modifiers, 0x00, keyCode, 0x00, 0x00, 0x00, 0x00, 0x00)
        logKeystroke(desc, "[ID:1 mod:0x%02X key:0x%02X (DOWN)]".format(modifiers, keyCode))

        if (targetDevice != null && hidDevice != null) {
            try {
                hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), pressReport)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending key down", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendKeyUp(modifiers: Byte = HidConstants.MOD_NONE, desc: String = "KeyUp") {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val releaseReport = byteArrayOf(modifiers, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        logKeystroke(desc, "[ID:1 mod:0x%02X (UP)]".format(modifiers))

        if (targetDevice != null && hidDevice != null) {
            try {
                hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), releaseReport)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending key up", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendConsumerKey(usageCode: Int, desc: String = "Media 0x%04X".format(usageCode)) {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val pressReport = byteArrayOf(
            (usageCode and 0xFF).toByte(),
            ((usageCode shr 8) and 0xFF).toByte()
        )
        val releaseReport = byteArrayOf(0x00, 0x00)
        logKeystroke(desc, "[ID:2 usage:0x%04X]".format(usageCode))

        if (targetDevice != null && hidDevice != null) {
            try {
                hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_CONSUMER.toInt(), pressReport)
                scope.launch {
                    delay(20)
                    hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_CONSUMER.toInt(), releaseReport)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending consumer key", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendMouse(buttons: Byte, dx: Byte, dy: Byte, wheel: Byte, desc: String = "Mouse move") {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val report = byteArrayOf(buttons, dx, dy, wheel)
        if (buttons != 0.toByte() || wheel != 0.toByte()) {
            logKeystroke(desc, "[ID:3 btn:$buttons dx:$dx dy:$dy whl:$wheel]")
        }

        if (targetDevice != null && hidDevice != null) {
            try {
                hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_MOUSE.toInt(), report)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending mouse report", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun sendTextStream(
        text: String,
        delayMs: Long = 10,
        extraModifier: Byte = HidConstants.MOD_NONE,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        val targetDevice = _connectedDevice.value?.bluetoothDevice
        val total = text.length

        for (i in text.indices) {
            val char = text[i]
            val mapping = HidConstants.charToHid(char)

            if (mapping != null) {
                val mod = (mapping.first.toInt() or extraModifier.toInt()).toByte()
                val keyCode = mapping.second
                val pressReport = byteArrayOf(mod, 0x00, keyCode, 0x00, 0x00, 0x00, 0x00, 0x00)
                val releaseReport = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

                logKeystroke("Char '$char'", "[ID:1 mod:0x%02X key:0x%02X]".format(mod, keyCode))

                if (targetDevice != null && hidDevice != null) {
                    hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), pressReport)
                    delay(delayMs.coerceAtLeast(8))
                    hidDevice?.sendReport(targetDevice, HidConstants.REPORT_ID_KEYBOARD.toInt(), releaseReport)
                    delay(delayMs.coerceAtLeast(8))
                } else {
                    // Preview / test delay
                    delay(delayMs.coerceAtLeast(8) * 2)
                }
            } else {
                logKeystroke("Unsupported Char '$char'", "[SKIPPED]", isSuccess = false)
            }
            onProgress(i + 1, total)
        }
    }

    fun clearLogs() {
        _keystrokeLogs.value = emptyList()
    }

    private fun logKeystroke(desc: String, rawBytes: String, isSuccess: Boolean = true) {
        val item = KeystrokeLogItem(
            timestamp = timeFormat.format(Date()),
            description = desc,
            rawBytes = rawBytes,
            isSuccess = isSuccess
        )
        val current = _keystrokeLogs.value.toMutableList()
        current.add(0, item)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _keystrokeLogs.value = current
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceInfo(device: BluetoothDevice): ConnectedDeviceInfo {
        val name = try {
            device.name ?: "Unknown Host (${device.address.takeLast(5)})"
        } catch (e: SecurityException) {
            "Host (${device.address.takeLast(5)})"
        }
        return ConnectedDeviceInfo(name = name, address = device.address, bluetoothDevice = device)
    }

    fun cleanup() {
        try {
            unregisterApp()
            if (bluetoothAdapter != null && hidDevice != null) {
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in cleanup", e)
        }
    }
}
