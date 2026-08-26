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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowUpward
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
fun SimplePhoneKeyboard(
    isShift: Boolean,
    isCapsLock: Boolean,
    onKeyChar: (Char) -> Unit,
    onSpecialKey: (Byte, String) -> Unit,
    onToggleShift: () -> Unit,
    onSwitchToNumbers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUpper = isShift || isCapsLock

    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
            .padding(6.dp)
            .testTag("simple_phone_keyboard"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: q w e r t y u i o p
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                PhoneLetterKey(
                    label = displayChar.toString(),
                    onClick = { onKeyChar(displayChar) }
                )
            }
        }

        // Row 2: a s d f g h j k l (with slight padding on sides for standard staggered look)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                PhoneLetterKey(
                    label = displayChar.toString(),
                    onClick = { onKeyChar(displayChar) }
                )
            }
        }

        // Row 3: Shift, z x c v b n m, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift / Caps key
            PhoneActionKey(
                icon = Icons.Default.ArrowUpward,
                isActive = isShift || isCapsLock,
                weight = 1.4f,
                tag = "phone_shift_key",
                onClick = onToggleShift
            )

            row3.forEach { ch ->
                val displayChar = if (isUpper) ch.uppercaseChar() else ch
                PhoneLetterKey(
                    label = displayChar.toString(),
                    onClick = { onKeyChar(displayChar) }
                )
            }

            // Backspace key
            PhoneActionKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                isActive = false,
                weight = 1.4f,
                tag = "phone_backspace_key",
                onClick = { onSpecialKey(HidConstants.KEY_BACKSPACE, "Backspace") }
            )
        }

        // Row 4: "123", comma, Space, period, Enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 123 switch button
            PhoneTextActionKey(
                label = "?123",
                weight = 1.4f,
                tag = "phone_to_numbers_btn",
                onClick = onSwitchToNumbers
            )

            // Comma key
            PhoneLetterKey(
                label = ",",
                weight = 0.9f,
                onClick = { onKeyChar(',') }
            )

            // Spacebar
            PhoneSpaceBar(
                weight = 4f,
                onClick = { onSpecialKey(HidConstants.KEY_SPACE, "Space") }
            )

            // Period key
            PhoneLetterKey(
                label = ".",
                weight = 0.9f,
                onClick = { onKeyChar('.') }
            )

            // Enter key
            PhoneActionKey(
                icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                isActive = true,
                isEnter = true,
                weight = 1.4f,
                tag = "phone_enter_key",
                onClick = { onSpecialKey(HidConstants.KEY_ENTER, "Enter") }
            )
        }
    }
}

@Composable
private fun RowScope.PhoneLetterKey(
    label: String,
    weight: Float = 1f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) KeyPressedLight else KeySurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPressed) PrimaryBlue else LightTextPrimary
        )
    }
}

@Composable
private fun RowScope.PhoneActionKey(
    icon: ImageVector,
    isActive: Boolean,
    isEnter: Boolean = false,
    weight: Float = 1f,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isPressed -> PrimaryBlueLight
        isEnter -> PrimaryBlue
        isActive -> PrimaryBlue
        else -> KeySpecialSurfaceLight
    }

    val iconColor = when {
        isPressed -> PrimaryBlue
        isEnter -> Color.White
        isActive -> Color.White
        else -> LightTextSecondary
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isActive || isEnter) PrimaryBlue else KeyBorderLight,
                RoundedCornerShape(8.dp)
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
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RowScope.PhoneTextActionKey(
    label: String,
    weight: Float = 1.4f,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) PrimaryBlueLight else KeySpecialSurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(8.dp))
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) PrimaryBlue else LightTextSecondary
        )
    }
}

@Composable
private fun RowScope.PhoneSpaceBar(
    weight: Float = 4f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) KeyPressedLight else KeySurfaceLight)
            .border(1.dp, KeyBorderLight, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("phone_spacebar"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Space",
            fontSize = 13.sp,
            color = if (isPressed) PrimaryBlue else LightTextMuted
        )
    }
}

