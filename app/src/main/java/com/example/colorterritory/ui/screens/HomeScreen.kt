package com.example.colorterritory.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.colorterritory.data.GameStats
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.components.PrimaryButton
import com.example.colorterritory.ui.components.ScreenScaffold
import com.example.colorterritory.ui.components.SecondaryButton
import com.example.colorterritory.ui.components.StatBlock
import com.example.colorterritory.ui.components.drawTerritoryTile
import com.example.colorterritory.ui.theme.LocalTerritoryPalette

@Composable
fun HomeScreen(
    stats: GameStats,
    hasMatchInProgress: Boolean,
    onPlay: () -> Unit,
    onResume: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    ScreenScaffold(title = null, onBack = null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Emblem()
                Spacer(Modifier.height(28.dp))
                Text(
                    "COLOR\nTERRITORY",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Choose a color. Expand your reach.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(40.dp))

                PrimaryButton("PLAY", onPlay, Modifier.fillMaxWidth())
                if (hasMatchInProgress) {
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("RESUME MATCH", onResume, Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("HOW TO PLAY", onHowToPlay, Modifier.weight(1.3f))
                    SecondaryButton("SETTINGS", onSettings, Modifier.weight(1f))
                    SecondaryButton("ABOUT", onAbout, Modifier.weight(0.9f))
                }

                Spacer(Modifier.height(40.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatBlock("Games", stats.gamesPlayed.toString(), Modifier.weight(1f))
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    StatBlock("P1 Wins", stats.player1Wins.toString(), Modifier.weight(1f))
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    StatBlock("P2 Wins", stats.player2Wins.toString(), Modifier.weight(1f))
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    StatBlock("Draws", stats.draws.toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

/** A 3×3 fragment of territory: copper and steel holding opposite corners. */
@Composable
private fun Emblem() {
    val palette = LocalTerritoryPalette.current
    val terrain = listOf(
        TerrainColor.PLUM, TerrainColor.SAND, TerrainColor.SLATE,
        TerrainColor.SAGE, TerrainColor.TERRACOTTA, TerrainColor.SAND,
        TerrainColor.SLATE, TerrainColor.SAGE, TerrainColor.PLUM
    )
    Canvas(Modifier.size(84.dp)) {
        val gap = size.width * 0.04f
        val side = (size.width - gap * 2) / 3
        for (i in 0 until 9) {
            val r = i / 3
            val c = i % 3
            val owner = when {
                (r == 2 && c == 0) || (r == 1 && c == 0) || (r == 2 && c == 1) -> TerritoryOwner.PLAYER_ONE
                r == 0 && c == 2 -> TerritoryOwner.PLAYER_TWO
                else -> TerritoryOwner.NONE
            }
            drawTerritoryTile(
                topLeft = Offset(c * (side + gap), r * (side + gap)),
                side = side,
                terrain = terrain[i],
                owner = owner,
                ownership = if (owner == TerritoryOwner.NONE) 0f else 1f,
                palette = palette
            )
        }
    }
}

