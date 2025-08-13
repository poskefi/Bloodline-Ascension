package com.webdevry.bloodlineascension.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StatBar(
    label: String,
    value: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    val pct = (value.coerceAtLeast(0).coerceIn(0, max).toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text("${(pct * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun ColorScheme.surfaceVariant(): Color =
    if (this.isLight) this.onSurface.copy(alpha = 0.08f) else this.onSurface.copy(alpha = 0.18f)

private val ColorScheme.isLight: Boolean
    get() = this.background.luminance() > 0.5f
