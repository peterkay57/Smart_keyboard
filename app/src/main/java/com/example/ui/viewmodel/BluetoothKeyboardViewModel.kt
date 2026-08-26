package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dictionary.WordSuggestionEngine
import com.example.data.hid.BluetoothHidManager
import com.example.data.hid.HidConstants
import com.example.data.model.ConnectedDeviceInfo
import com.example.data.model.HidConnectionState
import com.example.data.model.KeyboardTab
import com.example.data.model.KeystrokeLogItem
import com.example.data.model.LedStatus
import com.example.data.model.MacroSnippet
import com.example.data.model.ModifierState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class BluetoothKeyboardViewModel(application: Application) : AndroidViewModel(application) {

    val hidManager = BluetoothHidManager(application.applicationContext)
    val suggestionEngine = WordSuggestionEngine(application.applicationContext)

    val connectionState: StateFlow<HidConnectionState> = hidManager.connectionState
    val isAppRegistered: StateFlow<Boolean> = hidManager.isAppRegistered
    val connectedDevice: StateFlow<ConnectedDeviceInfo?> = hidManager.connectedDevice
    val pairedDevices: StateFlow<List<ConnectedDeviceInfo>> = hidManager.pairedDevices
    val ledStatus: StateFlow<LedStatus> = hidManager.ledStatus
    val keystrokeLogs: StateFlow<List<KeystrokeLogItem>> = hidManager.keystrokeLogs
    val lastErrorMessage: StateFlow<String?> = hidManager.lastErrorMessage
    val isHidSupported: StateFlow<Boolean> = hidManager.isHidSupported

    private val _selectedTab = MutableStateFlow(KeyboardTab.LIVE_TEXT)
    val selectedTab: StateFlow<KeyboardTab> = _selectedTab.asStateFlow()

    private val _keyboardMode = MutableStateFlow(com.example.data.model.KeyboardMode.PHONE)
    val keyboardMode: StateFlow<com.example.data.model.KeyboardMode> = _keyboardMode.asStateFlow()

    private val _isNumberMode = MutableStateFlow(false)
    val isNumberMode: StateFlow<Boolean> = _isNumberMode.asStateFlow()

    private val _isCapsLock = MutableStateFlow(false)
    val isCapsLock: StateFlow<Boolean> = _isCapsLock.asStateFlow()

    private val _modifierState = MutableStateFlow(ModifierState())
    val modifierState: StateFlow<ModifierState> = _modifierState.asStateFlow()

    private val _textBuffer = MutableStateFlow("")
    val textBuffer: StateFlow<String> = _textBuffer.asStateFlow()

    private val _lastTypedKey = MutableStateFlow<String>("")
    val lastTypedKey: StateFlow<String> = _lastTypedKey.asStateFlow()

    private val _composingWord = MutableStateFlow("")
    val composingWord: StateFlow<String> = _composingWord.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(suggestionEngine.getDefaultPredictions())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _isLiveTypingMode = MutableStateFlow(true)
    val isLiveTypingMode: StateFlow<Boolean> = _isLiveTypingMode.asStateFlow()

    private val _typingDelayMs = MutableStateFlow(12L)
    val typingDelayMs: StateFlow<Long> = _typingDelayMs.asStateFlow()

    private val _isSendingStream = MutableStateFlow(false)
    val isSendingStream: StateFlow<Boolean> = _isSendingStream.asStateFlow()

    private val _streamProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val streamProgress: StateFlow<Pair<Int, Int>?> = _streamProgress.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(true)
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _macroSnippets = MutableStateFlow<List<MacroSnippet>>(
        listOf(
            MacroSnippet("1", "Terminal: Update", "sudo apt update && sudo apt upgrade -y\n", "Terminal"),
            MacroSnippet("2", "Git: Status", "git status\n", "Git"),
            MacroSnippet("3", "Git: Commit & Push", "git commit -m \"Update\" && git push\n", "Git"),
            MacroSnippet("4", "Clear Screen", "clear\n", "Terminal"),
            MacroSnippet("5", "IP Address check", "curl ifconfig.me\n", "Network"),
            MacroSnippet("6", "Kill Port", "lsof -i :8080 | awk 'NR>1 {print $2}' | xargs kill -9\n", "Dev"),
            MacroSnippet("7", "Lorem Ipsum", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Text"),
            MacroSnippet("8", "Email Sign-off", "Best regards,\nSent via Android Bluetooth Keyboard", "Text")
        )
    )
    val macroSnippets: StateFlow<List<MacroSnippet>> = _macroSnippets.asStateFlow()

    private var streamingJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        hidManager.initialize()
    }

    fun setTab(tab: KeyboardTab) {
        _selectedTab.value = tab
    }

    fun setTextBuffer(text: String) {
        val oldText = _textBuffer.value
        _textBuffer.value = text

        // If Live typing mode is on and user typed a character at the end
        if (_isLiveTypingMode.value && text.length > oldText.length && text.startsWith(oldText)) {
            val addedChars = text.substring(oldText.length)
            sendTextImmediate(addedChars)
        } else if (_isLiveTypingMode.value && text.length < oldText.length) {
            // User backspaced
            val diff = oldText.length - text.length
            repeat(diff) {
                sendKeyPress(HidConstants.KEY_BACKSPACE, "Backspace")
            }
        }
        updateSuggestionsForCurrentBuffer()
    }

    fun toggleLiveTypingMode() {
        _isLiveTypingMode.value = !_isLiveTypingMode.value
    }

    fun setTypingDelay(delay: Long) {
        _typingDelayMs.value = delay
    }

    fun toggleHaptic() {
        _hapticFeedbackEnabled.value = !_hapticFeedbackEnabled.value
    }

    fun performHaptic(isHeavy: Boolean = false) {
        if (!_hapticFeedbackEnabled.value) return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect = if (isHeavy) {
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(8, 70)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {}
    }

    fun setKeyboardMode(mode: com.example.data.model.KeyboardMode) {
        performHaptic(true)
        _keyboardMode.value = mode
        _isNumberMode.value = false
    }

    fun toggleNumberMode() {
        performHaptic()
        _isNumberMode.value = !_isNumberMode.value
    }

    fun setNumberMode(enabled: Boolean) {
        performHaptic()
        _isNumberMode.value = enabled
    }

    fun toggleCapsLock() {
        performHaptic(true)
        _isCapsLock.value = !_isCapsLock.value
    }

    private fun extractTrailingWord(text: String): String {
        if (text.isEmpty()) return ""
        val lastChar = text.last()
        if (!lastChar.isLetterOrDigit() && lastChar != '\'') return ""
        var i = text.length - 1
        while (i >= 0 && (text[i].isLetterOrDigit() || text[i] == '\'')) {
            i--
        }
        return text.substring(i + 1)
    }

    private fun extractLastCompletedWord(text: String): String {
        val trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return ""
        val words = trimmed.split(Regex("[^a-zA-Z0-9']+")).filter { it.isNotBlank() }
        return words.lastOrNull() ?: ""
    }

    private fun updateSuggestionsForCurrentBuffer() {
        val currentText = _textBuffer.value
        val trailingWord = extractTrailingWord(currentText)
        if (trailingWord.isNotEmpty()) {
            _composingWord.value = trailingWord
            _suggestions.value = suggestionEngine.getSuggestions(trailingWord, 5)
            suggestionEngine.requestSpellCheck(trailingWord)
        } else {
            _composingWord.value = ""
            val lastWord = extractLastCompletedWord(currentText)
            _suggestions.value = if (lastWord.isNotEmpty()) {
                suggestionEngine.getNextWordPredictions(lastWord)
            } else {
                suggestionEngine.getDefaultPredictions()
            }
        }
    }

    fun typeCharacter(char: Char) {
        performHaptic()
        val isShift = _modifierState.value.shift
        val isCaps = _isCapsLock.value
        
        val effectiveChar = if (char.isLetter()) {
            if (isShift xor isCaps) char.uppercaseChar() else char.lowercaseChar()
        } else {
            char
        }

        _lastTypedKey.value = effectiveChar.toString()

        // Map character to HID report and send IMMEDIATELY
        val mapping = HidConstants.charToHid(effectiveChar)
        if (mapping != null) {
            val baseMod = mapping.first.toInt()
            val extraMod = (_modifierState.value.toByte().toInt() and 0xFD.toInt()) // remove shift bit if handled by charToHid
            val finalMod = (baseMod or extraMod).toByte()
            hidManager.sendKeyPress(mapping.second, finalMod, "Key '$effectiveChar'")
        }

        // Maintain internal buffer for dictionary word suggestions
        _textBuffer.value += effectiveChar

        // Update word suggestions
        updateSuggestionsForCurrentBuffer()

        // Reset single-shot shift
        if (_modifierState.value.shift) {
            _modifierState.value = _modifierState.value.copy(shift = false)
        }
    }

    fun selectSuggestion(suggestion: String) {
        performHaptic()
        val composing = _composingWord.value
        _lastTypedKey.value = suggestion
        
        if (composing.isNotEmpty()) {
            // Drop composing from buffer and append suggestion with trailing space
            val currentText = _textBuffer.value
            val newText = if (currentText.endsWith(composing)) {
                currentText.dropLast(composing.length) + "$suggestion "
            } else {
                currentText + "$suggestion "
            }
            _textBuffer.value = newText

            // On Host: send Backspaces for composing word length, then send suggestion + Space
            viewModelScope.launch {
                repeat(composing.length) {
                    hidManager.sendKeyPress(HidConstants.KEY_BACKSPACE, 0, "Backspace")
                    kotlinx.coroutines.delay(8)
                }
                hidManager.sendTextStream(
                    text = "$suggestion ",
                    delayMs = 8L,
                    extraModifier = _modifierState.value.toByte()
                )
            }
        } else {
            _textBuffer.value += "$suggestion "
            viewModelScope.launch {
                hidManager.sendTextStream(
                    text = "$suggestion ",
                    delayMs = 8L,
                    extraModifier = _modifierState.value.toByte()
                )
            }
        }

        _composingWord.value = ""
        _suggestions.value = suggestionEngine.getNextWordPredictions(suggestion)
    }

    fun typeSpecialKey(keyCode: Byte, desc: String) {
        performHaptic()
        _lastTypedKey.value = desc
        when (keyCode) {
            HidConstants.KEY_BACKSPACE -> {
                if (_textBuffer.value.isNotEmpty()) {
                    _textBuffer.value = _textBuffer.value.dropLast(1)
                }
                sendKeyPress(HidConstants.KEY_BACKSPACE, "Backspace")
                updateSuggestionsForCurrentBuffer()
            }
            HidConstants.KEY_ENTER -> {
                _textBuffer.value += "\n"
                sendKeyPress(HidConstants.KEY_ENTER, "Enter")
                _composingWord.value = ""
                _suggestions.value = suggestionEngine.getDefaultPredictions()
            }
            HidConstants.KEY_SPACE -> {
                val composing = _composingWord.value
                val topSuggestion = _suggestions.value.firstOrNull()
                
                // If user is actively typing a prefix and has a suggestion, pressing space auto-inserts the first suggestion
                if (composing.isNotEmpty() && topSuggestion != null && !topSuggestion.equals(composing, ignoreCase = true)) {
                    selectSuggestion(topSuggestion)
                } else {
                    _textBuffer.value += " "
                    sendKeyPress(HidConstants.KEY_SPACE, "Space")
                    _composingWord.value = ""
                    val lastWord = extractLastCompletedWord(_textBuffer.value)
                    _suggestions.value = if (lastWord.isNotEmpty()) {
                        suggestionEngine.getNextWordPredictions(lastWord)
                    } else {
                        suggestionEngine.getDefaultPredictions()
                    }
                }
            }
            HidConstants.KEY_TAB -> {
                _textBuffer.value += "\t"
                sendKeyPress(HidConstants.KEY_TAB, "Tab")
            }
            else -> {
                sendKeyPress(keyCode, desc)
            }
        }
    }

    // Toggle Sticky Modifiers
    fun toggleCtrl() {
        performHaptic(true)
        _modifierState.value = _modifierState.value.copy(ctrl = !_modifierState.value.ctrl)
    }

    fun toggleShift() {
        performHaptic(true)
        _modifierState.value = _modifierState.value.copy(shift = !_modifierState.value.shift)
    }

    fun toggleAlt() {
        performHaptic(true)
        _modifierState.value = _modifierState.value.copy(alt = !_modifierState.value.alt)
    }

    fun toggleGui() {
        performHaptic(true)
        _modifierState.value = _modifierState.value.copy(gui = !_modifierState.value.gui)
    }

    fun clearModifiers() {
        _modifierState.value = ModifierState()
    }

    fun sendKeyPress(keyCode: Byte, desc: String) {
        performHaptic()
        val currentMod = _modifierState.value.toByte()
        hidManager.sendKeyPress(keyCode, currentMod, desc)
        // Reset single-shot modifiers if desired or keep active
        if (_modifierState.value.shift && !_modifierState.value.ctrl && !_modifierState.value.alt && !_modifierState.value.gui) {
            // reset shift after one key press like standard virtual keyboards
            _modifierState.value = _modifierState.value.copy(shift = false)
        }
    }

    fun sendKeyDown(keyCode: Byte, desc: String) {
        performHaptic()
        val currentMod = _modifierState.value.toByte()
        hidManager.sendKeyDown(keyCode, currentMod, desc)
    }

    fun sendKeyUp(desc: String) {
        val currentMod = _modifierState.value.toByte()
        hidManager.sendKeyUp(currentMod, desc)
    }

    fun sendConsumerKey(usageCode: Int, desc: String) {
        performHaptic()
        hidManager.sendConsumerKey(usageCode, desc)
    }

    fun sendShortcut(name: String, keys: List<Byte>, modifiers: Byte) {
        performHaptic(true)
        viewModelScope.launch {
            for (key in keys) {
                hidManager.sendKeyPress(key, modifiers, name)
            }
        }
    }

    fun sendTextImmediate(text: String) {
        performHaptic()
        viewModelScope.launch {
            hidManager.sendTextStream(
                text = text,
                delayMs = _typingDelayMs.value,
                extraModifier = _modifierState.value.toByte()
            )
        }
    }

    fun sendBufferToHost() {
        val textToSend = _textBuffer.value
        if (textToSend.isEmpty()) return

        performHaptic(true)
        streamingJob?.cancel()
        _isSendingStream.value = true
        _streamProgress.value = Pair(0, textToSend.length)

        streamingJob = viewModelScope.launch {
            try {
                hidManager.sendTextStream(
                    text = textToSend,
                    delayMs = _typingDelayMs.value,
                    extraModifier = _modifierState.value.toByte(),
                    onProgress = { current, total ->
                        _streamProgress.value = Pair(current, total)
                    }
                )
            } finally {
                _isSendingStream.value = false
                _streamProgress.value = null
            }
        }
    }

    fun cancelStream() {
        streamingJob?.cancel()
        _isSendingStream.value = false
        _streamProgress.value = null
        performHaptic(true)
    }

    fun clearBuffer() {
        performHaptic()
        _textBuffer.value = ""
        _composingWord.value = ""
        _suggestions.value = suggestionEngine.getDefaultPredictions()
    }

    // Touchpad & Mouse
    fun onTouchpadMove(dx: Float, dy: Float) {
        val scale = 1.8f
        val scaledDx = (dx * scale).coerceIn(-127f, 127f).toInt().toByte()
        val scaledDy = (dy * scale).coerceIn(-127f, 127f).toInt().toByte()
        hidManager.sendMouse(buttons = 0, dx = scaledDx, dy = scaledDy, wheel = 0)
    }

    fun onTouchpadClick(buttonMask: Byte, desc: String = "Mouse Click") {
        performHaptic(true)
        viewModelScope.launch {
            hidManager.sendMouse(buttons = buttonMask, dx = 0, dy = 0, wheel = 0, desc = "$desc Down")
            kotlinx.coroutines.delay(25)
            hidManager.sendMouse(buttons = 0, dx = 0, dy = 0, wheel = 0, desc = "$desc Up")
        }
    }

    fun onTouchpadScroll(deltaY: Float) {
        val wheel = (deltaY.coerceIn(-127f, 127f)).toInt().toByte()
        hidManager.sendMouse(buttons = 0, dx = 0, dy = 0, wheel = wheel, desc = "Mouse Scroll")
    }

    // Macro management
    fun addMacro(title: String, text: String, category: String = "Custom") {
        val newMacro = MacroSnippet(UUID.randomUUID().toString(), title, text, category)
        _macroSnippets.value = _macroSnippets.value + newMacro
    }

    fun deleteMacro(id: String) {
        _macroSnippets.value = _macroSnippets.value.filter { it.id != id }
    }

    fun sendMacro(macro: MacroSnippet) {
        performHaptic(true)
        viewModelScope.launch {
            _isSendingStream.value = true
            _streamProgress.value = Pair(0, macro.text.length)
            try {
                hidManager.sendTextStream(
                    text = macro.text,
                    delayMs = _typingDelayMs.value,
                    onProgress = { current, total ->
                        _streamProgress.value = Pair(current, total)
                    }
                )
            } finally {
                _isSendingStream.value = false
                _streamProgress.value = null
            }
        }
    }

    // Device & Connection management
    fun connectToDevice(device: ConnectedDeviceInfo) {
        performHaptic(true)
        device.bluetoothDevice?.let { hidManager.connectToDevice(it) }
    }

    fun disconnectDevice() {
        performHaptic(true)
        hidManager.disconnectDevice()
    }

    fun refreshBluetooth() {
        hidManager.initialize()
        hidManager.refreshPairedDevices()
    }

    fun clearLogs() {
        hidManager.clearLogs()
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
        suggestionEngine.close()
        hidManager.cleanup()
    }
}
