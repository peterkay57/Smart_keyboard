package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.HidConnectionState
import com.example.data.model.KeyboardMode
import com.example.ui.components.BluetoothSettingsDialog
import com.example.ui.components.SimpleLaptopKeyboard
import com.example.ui.components.SimpleNumberKeyboard
import com.example.ui.components.SimplePhoneKeyboard
import com.example.ui.components.WordSuggestionStrip
import com.example.ui.theme.KeyBorderLight
import com.example.ui.theme.KeySpecialSurfaceLight
import com.example.ui.theme.KeySurfaceLight
import com.example.ui.theme.LightBg
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.BluetoothKeyboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothKeyboardApp(
    viewModel: BluetoothKeyboardViewModel = viewModel()
) {
    val context = LocalContext.current

    val connectionState by viewModel.connectionState.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val isAppRegistered by viewModel.isAppRegistered.collectAsState()

    val textBuffer by viewModel.textBuffer.collectAsState()
    val keyboardMode by viewModel.keyboardMode.collectAsState()
    val isNumberMode by viewModel.isNumberMode.collectAsState()
    val isCapsLock by viewModel.isCapsLock.collectAsState()
    val modifierState by viewModel.modifierState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Bluetooth Permission Handling
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    var hasPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        if (hasPermissions) {
            viewModel.refreshBluetooth()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Bluetooth Keyboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LightTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Bluetooth Settings",
                            tint = LightTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBg
                )
            )
        },
        containerColor = LightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permission Alert Banner if needed
            if (!hasPermissions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(RoseError.copy(alpha = 0.4f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = RoseError
                            )
                            Text(
                                text = "Bluetooth permission is required to act as a wireless keyboard.",
                                fontSize = 12.sp,
                                color = LightTextPrimary
                            )
                        }
                        Button(
                            onClick = { permissionLauncher.launch(requiredPermissions) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_permission_btn")
                        ) {
                            Text("Grant", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // 1. TEXT AREA (Top) - Big, clean, white background, placeholder "writes anything"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("text_input_area_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(LightBorder))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = textBuffer,
                        onValueChange = { viewModel.setTextBuffer(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("main_text_field"),
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(PrimaryBlue),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (textBuffer.isEmpty()) {
                                    Text(
                                        text = "writes anything",
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        color = LightTextMuted,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Clear button if textBuffer is not empty
                    if (textBuffer.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setTextBuffer("") },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(KeySpecialSurfaceLight)
                                .testTag("clear_text_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                tint = LightTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. MODE SWITCHER (Above the suggestion word line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightSurface)
                    .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp)
                    .testTag("mode_switch_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Phone Keyboard Button
                val isPhoneActive = keyboardMode == KeyboardMode.PHONE && !isNumberMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isPhoneActive) PrimaryBlue
                            else Color.Transparent
                        )
                        .clickable { viewModel.setKeyboardMode(KeyboardMode.PHONE) }
                        .testTag("mode_phone_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isPhoneActive) Color.White else LightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Phone Keyboard",
                            fontWeight = if (isPhoneActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isPhoneActive) Color.White else LightTextSecondary
                        )
                    }
                }

                // Computer Keyboard Button
                val isLaptopActive = keyboardMode == KeyboardMode.LAPTOP && !isNumberMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isLaptopActive) PrimaryBlue
                            else Color.Transparent
                        )
                        .clickable { viewModel.setKeyboardMode(KeyboardMode.LAPTOP) }
                        .testTag("mode_laptop_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = if (isLaptopActive) Color.White else LightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Computer Keyboard",
                            fontWeight = if (isLaptopActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isLaptopActive) Color.White else LightTextSecondary
                        )
                    }
                }
            }

            // 3. WORD SUGGESTION LINE (Directly above the QWERTY row)
            WordSuggestionStrip(
                suggestions = suggestions,
                onSuggestionSelected = { viewModel.selectSuggestion(it) }
            )

            // 4. KEYBOARD LAYOUT (Bottom - begins with q w e r t y u i o p line)
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (isNumberMode) {
                    SimpleNumberKeyboard(
                        onKeyChar = { viewModel.typeCharacter(it) },
                        onSpecialKey = { code, desc -> viewModel.typeSpecialKey(code, desc) },
                        onReturnToLetters = { viewModel.setNumberMode(false) }
                    )
                } else {
                    when (keyboardMode) {
                        KeyboardMode.PHONE -> {
                            SimplePhoneKeyboard(
                                isShift = modifierState.shift,
                                isCapsLock = isCapsLock,
                                onKeyChar = { viewModel.typeCharacter(it) },
                                onSpecialKey = { code, desc -> viewModel.typeSpecialKey(code, desc) },
                                onToggleShift = { viewModel.toggleShift() },
                                onSwitchToNumbers = { viewModel.setNumberMode(true) }
                            )
                        }
                        KeyboardMode.LAPTOP -> {
                            SimpleLaptopKeyboard(
                                modifierState = modifierState,
                                isCapsLock = isCapsLock,
                                onKeyChar = { viewModel.typeCharacter(it) },
                                onSpecialKey = { code, desc -> viewModel.typeSpecialKey(code, desc) },
                                onToggleCtrl = { viewModel.toggleCtrl() },
                                onToggleAlt = { viewModel.toggleAlt() },
                                onToggleShift = { viewModel.toggleShift() },
                                onToggleGui = { viewModel.toggleGui() },
                                onToggleCapsLock = { viewModel.toggleCapsLock() },
                                onSwitchToNumbers = { viewModel.setNumberMode(true) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Bluetooth Settings Dialog
    if (showSettingsDialog) {
        BluetoothSettingsDialog(
            connectionState = connectionState,
            connectedDevice = connectedDevice,
            pairedDevices = pairedDevices,
            isAppRegistered = isAppRegistered,
            onDismiss = { showSettingsDialog = false },
            onConnectToDevice = { viewModel.connectToDevice(it) },
            onDisconnect = { viewModel.disconnectDevice() },
            onRefresh = { viewModel.refreshBluetooth() }
        )
    }
}
