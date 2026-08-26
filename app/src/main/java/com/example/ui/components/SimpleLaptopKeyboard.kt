package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.hid.HidConstants
import com.example.data.model.ModifierState
import com.example.ui.theme.KeyBorderLight
import com.example.ui.theme.KeyPressedLight
import com.example.ui.theme.KeySpecialSurfaceLight
import com.example.ui.theme.KeySurfaceLight
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight

@Composable
fun SimpleLaptopKeyboard(
    modifierState: ModifierState,
    isCapsLock: Boolean,
    onKeyChar: (Char) -> Unit,
    onSpecialKey: (Byte, String) -> Unit,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleGui: () -> Unit,
    onToggleCapsLock: () -> Unit,
    onSwitchToNumbers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUpper = modifierState.shift || isCapsLock

    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Row 1: Esc, q w e r t y u i o p, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LaptopSpecialKey(
                label = "Esc",
                weight = 1.1f,
                tag = "laptop_esc_key",
                onClick = { onSpecialKey(HidConstants.KEY_ESC, "Escape") }
            )

            row1.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                LaptopLetterKey(
                    label = displayChar.toString(),
                    weight = 1f,
                    onClick = { onKeyChar(displayChar) }
                )
            }

            LaptopActionIconKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                weight = 1.3f,
                tag = "laptop_backspace_key",
                onClick = { onSpecialKey(HidConstants.KEY_BACKSPACE, "Backspace") }
            )
        }

        // Row 2: Tab, a s d f g h j k l, Enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LaptopSpecialKey(
                label = "Tab",
                weight = 1.2f,
                tag = "laptop_tab_key",
                onClick = { onSpecialKey(HidConstants.KEY_TAB, "Tab") }
            )

            row2.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                LaptopLetterKey(
                    label = displayChar.toString(),
                    weight = 1f,
                    onClick = { onKeyChar(displayChar) }
                )
            }

            LaptopActionIconKey(
                icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                weight = 1.4f,
                isAccent = true,
                tag = "laptop_enter_key",
                onClick = { onSpecialKey(HidConstants.KEY_ENTER, "Enter") }
            )
        }

        // Row 3: Caps Lock, Shift, z x c v b n m, Shift
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Caps Lock key
            LaptopToggleKey(
                label = "Caps",
                isActive = isCapsLock,
                weight = 1.3f,
                tag = "laptop_caps_key",
                onClick = onToggleCapsLock
            )

            // Left Shift
            LaptopToggleKey(
                label = "Shift",
                isActive = modifierState.shift,
                weight = 1.3f,
                tag = "laptop_shift_key",
                onClick = onToggleShift
            )

            row3.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                LaptopLetterKey(
                    label = displayChar.toString(),
                    weight = 1f,
                    onClick = { onKeyChar(displayChar) }
                )
            }

            // "123" Switch Key
            LaptopSpecialKey(
                label = "123",
                weight = 1.3f,
                isHighlight = true,
                tag = "laptop_to_numbers_btn",
                onClick = onSwitchToNumbers
            )
        }

        // Row 4: Ctrl, Win/Cmd, Alt, Space, Arrow Keys (← ↑ ↓ →)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ctrl
            LaptopToggleKey(
                label = "Ctrl",
                isActive = modifierState.ctrl,
                weight = 1.2f,
                tag = "laptop_ctrl_key",
                onClick = onToggleCtrl
            )

            // Win / Cmd
            LaptopToggleKey(
                label = "Win",
                isActive = modifierState.gui,
                weight = 1.1f,
                tag = "laptop_win_key",
                onClick = onToggleGui
            )

            // Alt
            LaptopToggleKey(
                label = "Alt",
                isActive = modifierState.alt,
                weight = 1.1f,
                tag = "laptop_alt_key",
                onClick = onToggleAlt
            )

            // Spacebar
            LaptopSpaceBar(
                weight = 3.6f,
                onClick = { onSpecialKey(HidConstants.KEY_SPACE, "Space") }
            )

            // Arrow Keys Cluster (Left, Up, Down, Right)
            LaptopArrowKey(
                icon = Icons.Default.KeyboardArrowLeft,
                desc = "Left",
                code = HidConstants.KEY_LEFT_ARROW,
                onSpecialKey = onSpecialKey
            )
            LaptopArrowKey(
                icon = Icons.Default.KeyboardArrowUp,
                desc = "Up",
                code = HidConstants.KEY_UP_ARROW,
                onSpecialKey = onSpecialKey
            )
            LaptopArrowKey(
                icon = Icons.Default.KeyboardArrowDown,
                desc = "Down",
                code = HidConstants.KEY_DOWN_ARROW,
                onSpecialKey = onSpecialKey
            )
            LaptopArrowKey(
                icon = Icons.Default.KeyboardArrowRight,
                desc = "Right",
                code = HidConstants.KEY_RIGHT_ARROW,
                onSpecialKey = onSpecialKey
            )
        }
    }
}

@Composable
private fun RowScope.LaptopLetterKey(
    label: String,
    weight: Float = 1f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) KeyPressedLight else KeySurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("laptop_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPressed) PrimaryBlue else LightTextPrimary
        )
    }
}

@Composable
private fun RowScope.LaptopSpecialKey(
    label: String,
    weight: Float = 1f,
    isHighlight: Boolean = false,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isPressed -> PrimaryBlueLight
        isHighlight -> PrimaryBlue
        else -> KeySpecialSurfaceLight
    }

    val textColor = when {
        isPressed -> PrimaryBlue
        isHighlight -> Color.White
        else -> LightTextSecondary
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isHighlight) PrimaryBlue else KeyBorderLight,
                RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun RowScope.LaptopToggleKey(
    label: String,
    isActive: Boolean,
    weight: Float = 1f,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isPressed -> PrimaryBlueLight
        isActive -> PrimaryBlue
        else -> KeySpecialSurfaceLight
    }

    val textColor = when {
        isPressed -> PrimaryBlue
        isActive -> Color.White
        else -> LightTextSecondary
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isActive) PrimaryBlue else KeyBorderLight,
                RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun RowScope.LaptopActionIconKey(
    icon: ImageVector,
    weight: Float = 1f,
    isAccent: Boolean = false,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isPressed -> PrimaryBlueLight
        isAccent -> PrimaryBlue
        else -> KeySpecialSurfaceLight
    }

    val iconColor = when {
        isPressed -> PrimaryBlue
        isAccent -> Color.White
        else -> LightTextSecondary
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isAccent) PrimaryBlue else KeyBorderLight,
                RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RowScope.LaptopSpaceBar(
    weight: Float = 3.6f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) KeyPressedLight else KeySurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("laptop_spacebar"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Space",
            fontSize = 12.sp,
            color = if (isPressed) PrimaryBlue else LightTextMuted
        )
    }
}

@Composable
private fun RowScope.LaptopArrowKey(
    icon: ImageVector,
    desc: String,
    code: Byte,
    onSpecialKey: (Byte, String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(0.9f)
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) KeyPressedLight else KeySurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onSpecialKey(code, "Arrow $desc")
            }
            .testTag("laptop_arrow_$desc"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = if (isPressed) PrimaryBlue else LightTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
