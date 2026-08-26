package com.example.data.model

import android.bluetooth.BluetoothDevice

enum class HidConnectionState {
    NOT_INITIALIZED,
    BLUETOOTH_DISABLED,
    NO_PERMISSIONS,
    UNSUPPORTED,
    READY_UNPAIRED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

data class ConnectedDeviceInfo(
    val name: String,
    val address: String,
    val bluetoothDevice: BluetoothDevice? = null
)

data class LedStatus(
    val numLock: Boolean = false,
    val capsLock: Boolean = false,
    val scrollLock: Boolean = false
)

data class KeystrokeLogItem(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val description: String,
    val rawBytes: String,
    val isSuccess: Boolean = true
)

data class MacroSnippet(
    val id: String,
    val title: String,
    val text: String,
    val category: String = "General"
)

enum class KeyboardMode {
    PHONE,
    LAPTOP
}

enum class KeyboardTab {
    LIVE_TEXT,
    VIRTUAL_KEYBOARD,
    NAV_FUNCTION,
    MEDIA_CONTROLS,
    TOUCHPAD,
    MACROS
}

data class ModifierState(
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
    val gui: Boolean = false // Windows / Command key
) {
    fun toByte(): Byte {
        var mod: Int = 0
        if (ctrl) mod = mod or 0x01
        if (shift) mod = mod or 0x02
        if (alt) mod = mod or 0x04
        if (gui) mod = mod or 0x08
        return mod.toByte()
    }

    val isActive: Boolean
        get() = ctrl || shift || alt || gui
}
