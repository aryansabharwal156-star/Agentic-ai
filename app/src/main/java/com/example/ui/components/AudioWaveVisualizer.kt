package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HighDensityAccentPurple
import com.example.ui.theme.HighDensityPrimary

@Composable
fun AudioWaveVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    barMaxHeight: Dp = 48.dp,
    isActive: Boolean = true,
    activeBrush: Brush = Brush.verticalGradient(
        listOf(HighDensityAccentPurple, HighDensityPrimary)
    ),
    idleColor: Color = HighDensityAccentPurple.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier.height(barMaxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val animatedHeight = remember { Animatable(0.2f) }

            LaunchedEffect(amp, isActive) {
                if (isActive) {
                    animatedHeight.animateTo(
                        targetValue = amp.coerceIn(0.15f, 1.0f),
                        animationSpec = tween(durationMillis = 80)
                    )
                } else {
                    animatedHeight.animateTo(
                        targetValue = 0.15f,
                        animationSpec = tween(durationMillis = 200)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animatedHeight.value)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isActive) Modifier.background(activeBrush)
                        else Modifier.background(idleColor)
                    )
            )
        }
    }
}
