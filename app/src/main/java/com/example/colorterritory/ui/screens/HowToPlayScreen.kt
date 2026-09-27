package com.example.colorterritory.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.components.CenteredColumn
import com.example.colorterritory.ui.components.ScreenScaffold
import com.example.colorterritory.ui.components.SectionLabel
import com.example.colorterritory.ui.components.drawTerritoryTile
import com.example.colorterritory.ui.theme.LocalTerritoryPalette

private val Rules = listOf(
    "Each player begins in an opposite corner — Player 1 bottom-left, Player 2 top-right.",
    "On your turn, choose one terrain color. You never tap the board.",
    "Every neutral tile of that color touching your territory is claimed — and so is every same-color tile connected to those.",
    "Connections run up, down, left and right. Never diagonally.",
    "Opponent territory can't be captured. It blocks your expansion.",
    "Colors that would claim nothing are unavailable. With no available color, your turn passes automatically.",
    "Choose colors that open the way to larger regions. When the board is filled, the player holding more tiles wins."
)

private val ExampleRows = listOf("SPTB", "GGSP", "TGBS", "BGGT")
private val ExampleClaimed = setOf(1 to 0, 1 to 1, 2 to 1, 3 to 1, 3 to 2)

private fun exampleTerrain(code: Char) = when (code) {
    'T' -> TerrainColor.TERRACOTTA
    'G' -> TerrainColor.SAGE
    'S' -> TerrainColor.SAND
    'B' -> TerrainColor.SLATE
    else -> TerrainColor.PLUM
}

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    ScreenScaffold(title = "How to Play", onBack = onBack) {
        CenteredColumn(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            Rules.forEachIndexed { index, rule ->
                Row(Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "%02d".format(index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalTerritoryPalette.current.playerOne.accent,
                        modifier = Modifier.width(34.dp).padding(top = 2.dp)
                    )
                    Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Example — Player 1 chooses Sage")
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        contentDescription = "Example. Player 1 owns the bottom-left tile. Choosing Sage claims " +
                            "five connected Sage tiles in one turn."
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExampleBoard(claimed = false, label = "BEFORE", modifier = Modifier.weight(1f))
                Arrow()
                ExampleBoard(claimed = true, label = "AFTER", modifier = Modifier.weight(1f))
            }
            Text(
                "The Sage tile beside the corner is claimed, and the expansion flows on through every connected Sage tile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel("Terrain symbols")
            Spacer(Modifier.height(12.dp))
            TerrainColor.entries.forEach { terrain -> TerrainLegendRow(terrain) }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExampleBoard(claimed: Boolean, label: String, modifier: Modifier) {
    val palette = LocalTerritoryPalette.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val n = 4
            val gap = size.width * 0.03f
            val side = (size.width - gap * (n - 1)) / n
            for (r in 0 until n) for (c in 0 until n) {
                val owned = (r == n - 1 && c == 0) || (claimed && (r to c) in ExampleClaimed)
                drawTerritoryTile(
                    topLeft = Offset(c * (side + gap), r * (side + gap)),
                    side = side,
                    terrain = exampleTerrain(ExampleRows[r][c]),
                    owner = if (owned) TerritoryOwner.PLAYER_ONE else TerritoryOwner.NONE,
                    ownership = if (owned) 1f else 0f,
                    palette = palette,
                    highlight = if (claimed && (r to c) in ExampleClaimed) 1f else 0f
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Arrow() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(20.dp)) {
        val stroke = 1.6.dp.toPx()
        val w = size.width
        drawLine(color, Offset(w * 0.1f, w * 0.5f), Offset(w * 0.9f, w * 0.5f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.6f, w * 0.22f), Offset(w * 0.9f, w * 0.5f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.6f, w * 0.78f), Offset(w * 0.9f, w * 0.5f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun TerrainLegendRow(terrain: TerrainColor) {
    val palette = LocalTerritoryPalette.current
    Row(
        Modifier.padding(vertical = 6.dp).clearAndSetSemantics {
            contentDescription = "${terrain.displayName}: ${terrain.symbolName} symbol"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(28.dp)) {
            drawTerritoryTile(Offset.Zero, size.minDimension, terrain, TerritoryOwner.NONE, 0f, palette)
        }
        Spacer(Modifier.width(14.dp))
        Text(terrain.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Text(terrain.symbolName.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
