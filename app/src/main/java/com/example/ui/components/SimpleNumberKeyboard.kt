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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
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
fun SimpleNumberKeyboard(
    onKeyChar: (Char) -> Unit,
    onSpecialKey: (Byte, String) -> Unit,
    onReturnToLetters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    val row2 = listOf('@', '#', '$', '%', '&', '-', '+', '(', ')')
    val row3 = listOf('*', '"', '\'', ':', ';', '!', '?', '/')

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
            .padding(6.dp)
            .testTag("simple_number_keyboard"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Numbers 1-0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { ch ->
                NumKey(
                    label = ch.toString(),
                    isNum = true,
                    onClick = { onKeyChar(ch) }
                )
            }
        }

        // Row 2: Common symbols
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { ch ->
                NumKey(
                    label = ch.toString(),
                    isNum = false,
                    onClick = { onKeyChar(ch) }
                )
            }
            NumKey(
                label = "=",
                isNum = false,
                onClick = { onKeyChar('=') }
            )
        }

        // Row 3: Symbols + Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row3.forEach { ch ->
                NumKey(
                    label = ch.toString(),
                    isNum = false,
                    onClick = { onKeyChar(ch) }
                )
            }

            // Backspace key
            NumActionIconKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                weight = 1.3f,
                tag = "num_backspace_key",
                onClick = { onSpecialKey(HidConstants.KEY_BACKSPACE, "Backspace") }
            )
        }

        // Row 4: "ABC" (return), comma, Space, period, Enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ABC button
            NumTextActionKey(
                label = "ABC",
                weight = 1.5f,
                tag = "num_to_letters_btn",
                onClick = onReturnToLetters
            )

            // Comma
            NumKey(
                label = ",",
                weight = 0.9f,
                isNum = false,
                onClick = { onKeyChar(',') }
            )

            // Spacebar
            NumSpaceBar(
                weight = 4f,
                onClick = { onSpecialKey(HidConstants.KEY_SPACE, "Space") }
            )

            // Period
            NumKey(
                label = ".",
                weight = 0.9f,
                isNum = false,
                onClick = { onKeyChar('.') }
            )

            // Enter key
            NumActionIconKey(
                icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                isAccent = true,
                weight = 1.4f,
                tag = "num_enter_key",
                onClick = { onSpecialKey(HidConstants.KEY_ENTER, "Enter") }
            )
        }
    }
}

@Composable
private fun RowScope.NumKey(
    label: String,
    weight: Float = 1f,
    isNum: Boolean = false,
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
            .testTag("num_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (isNum) 20.sp else 18.sp,
            fontWeight = if (isNum) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isPressed) PrimaryBlue else LightTextPrimary
        )
    }
}

@Composable
private fun RowScope.NumTextActionKey(
    label: String,
    weight: Float = 1.5f,
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
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) PrimaryBlue else LightTextSecondary
        )
    }
}

@Composable
private fun RowScope.NumActionIconKey(
    icon: ImageVector,
    isAccent: Boolean = false,
    weight: Float = 1f,
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
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isAccent) PrimaryBlue else KeyBorderLight,
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
private fun RowScope.NumSpaceBar(
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
            .testTag("num_spacebar"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Space",
            fontSize = 13.sp,
            color = if (isPressed) PrimaryBlue else LightTextMuted
        )
    }
}

