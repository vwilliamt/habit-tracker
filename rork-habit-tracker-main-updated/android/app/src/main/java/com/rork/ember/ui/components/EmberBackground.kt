package com.rork.ember.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import com.rork.ember.ui.theme.EmberColors

/** Full-screen atmospheric background: deep charcoal with a warm radial glow up top. */
@Composable
fun EmberBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EmberColors.Background)
            .drawBehind {
                // Upper warm glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmberColors.GlowTop,
                            EmberColors.GlowTopMid,
                            Color(0x00000000),
                        ),
                        center = Offset(size.width * 0.5f, -size.height * 0.05f),
                        radius = size.width * 0.95f,
                    ),
                    size = Size(size.width, size.height),
                )
                // Subtle lower glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmberColors.GlowBottom,
                            Color(0x00000000),
                        ),
                        center = Offset(size.width * 0.15f, size.height * 1.0f),
                        radius = size.width * 0.7f,
                    ),
                    size = Size(size.width, size.height),
                )
            },
    ) { content() }
}

@Suppress("unused")
private fun strokeMarker() = Stroke(width = 1f) // keep import
