package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight

@Composable
fun WordSuggestionStrip(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Default fallback suggestions if none yet
    val displayList = if (suggestions.isEmpty()) {
        listOf("hello", "hi", "how", "have")
    } else {
        suggestions.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LightSurface)
            .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("word_suggestion_strip"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val primaryIndex = if (displayList.size >= 3) 1 else 0

            displayList.forEachIndexed { index, word ->
                val isPrimary = index == primaryIndex || (displayList.size < 3 && index == 0)

                SuggestionItem(
                    text = word,
                    isPrimary = isPrimary,
                    index = index,
                    onClick = { onSuggestionSelected(word) }
                )

                if (index < displayList.size - 1) {
                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .width(1.dp),
                        color = LightBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SuggestionItem(
    text: String,
    isPrimary: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressed) PrimaryBlueLight
                else if (isPrimary) PrimaryBlue.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("suggestion_chip_$index"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isPrimary) 15.sp else 14.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
            color = if (isPressed) PrimaryBlue else if (isPrimary) PrimaryBlue else LightTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .testTag("suggestion_text_$index")
        )
    }
}

