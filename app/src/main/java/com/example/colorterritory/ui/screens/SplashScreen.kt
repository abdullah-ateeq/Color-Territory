package com.example.colorterritory.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.components.drawTerrainSymbol
import com.example.colorterritory.ui.components.drawTerritoryTile
import com.example.colorterritory.ui.components.symbolInk
import com.example.colorterritory.ui.theme.LocalTerritoryPalette
import kotlinx.coroutines.delay

private val SplashRows = listOf("PTSBG", "SBPTS", "TGSPB", "GGTSP", "BGPTS")

/** Sage tiles claimed by Player 1 in the splash, by BFS layer. */
private val SplashClaimLayers = listOf(
    listOf(3 to 0, 4 to 1),
    listOf(3 to 1),
    listOf(2 to 1)
)

private fun splashTerrain(code: Char) = when (code) {
    'T' -> TerrainColor.TERRACOTTA
    'G' -> TerrainColor.SAGE
    'S' -> TerrainColor.SAND
    'B' -> TerrainColor.SLATE
    else -> TerrainColor.PLUM
}

private fun phase(t: Float, start: Float, end: Float) = ((t - start) / (end - start)).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    val palette = LocalTerritoryPalette.current
    val background = MaterialTheme.colorScheme.background

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1450, easing = LinearEasing))
        delay(120)
        finish()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Color Territory" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(180.dp)) {
            val t = progress.value
            val n = 5
            val gap = size.width * 0.02f
            val side = (size.width - gap * (n - 1)) / n
            val cornerOwnership = phase(t, 0.18f, 0.36f)
            for (r in 0 until n) for (c in 0 until n) {
                val topLeft = Offset(c * (side + gap), r * (side + gap))
                val layerIndex = SplashClaimLayers.indexOfFirst { layer -> (r to c) in layer }
                val (owner, ownership) = when {
                    r == n - 1 && c == 0 -> TerritoryOwner.PLAYER_ONE to cornerOwnership
                    r == 0 && c == n - 1 -> TerritoryOwner.PLAYER_TWO to cornerOwnership
                    layerIndex >= 0 -> {
                        val start = 0.55f + layerIndex * 0.07f
                        TerritoryOwner.PLAYER_ONE to phase(t, start, start + 0.12f)
                    }
                    else -> TerritoryOwner.NONE to 0f
                }
                drawTerritoryTile(
                    topLeft = topLeft,
                    side = side,
                    terrain = splashTerrain(SplashRows[r][c]),
                    owner = owner,
                    ownership = ownership,
                    palette = palette
                )
                // Tiles fade in diagonally from the bottom-left corner.
                val appear = phase(t, 0.02f * ((n - 1 - r) + c), 0.02f * ((n - 1 - r) + c) + 0.14f)
                if (appear < 1f) {
                    drawRect(background, topLeft, Size(side + gap, side + gap), alpha = 1f - appear)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        // Colour chip that pulses to "select" Sage.
        Canvas(
            Modifier
                .size(34.dp)
                .graphicsLayer {
                    val t = progress.value
                    val appear = phase(t, 0.3f, 0.4f)
                    val pulse = phase(t, 0.4f, 0.6f)
                    val scale = 1f + 0.18f * kotlin.math.sin(pulse * Math.PI).toFloat()
                    alpha = appear
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            val base = palette.terrainColor(TerrainColor.SAGE)
            drawRoundRect(base, cornerRadius = CornerRadius(size.minDimension * 0.24f))
            drawRoundRect(
                palette.playerOne.accent,
                cornerRadius = CornerRadius(size.minDimension * 0.24f),
                style = Stroke(1.5.dp.toPx()),
                alpha = phase(progress.value, 0.4f, 0.5f) * (1f - phase(progress.value, 0.7f, 0.85f))
            )
            drawTerrainSymbol(TerrainColor.SAGE, center, size.minDimension * 0.32f, symbolInk(base))
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = "COLOR TERRITORY",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val a = phase(progress.value, 0.62f, 0.92f)
                alpha = a
                translationY = (1f - a) * 12.dp.toPx()
            }
        )
    }
}
