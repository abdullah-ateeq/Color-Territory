package com.example.colorterritory.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.ui.theme.LocalTerritoryPalette

private const val TABULAR = "tnum"

@Composable
fun PlayerPanel(
    player: Player,
    score: Int,
    isTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalTerritoryPalette.current
    val tone = palette.tone(player)
    val shape = RoundedCornerShape(14.dp)
    val activeAlpha by animateFloatAsState(if (isTurn) 1f else 0f, tween(220), label = "panelActive")
    val label = if (player == Player.PLAYER_ONE) "PLAYER 1" else "PLAYER 2"
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .border(1.dp, tone.accent.copy(alpha = 0.75f * activeAlpha), shape)
            .clearAndSetSemantics {
                contentDescription = "${player.displayName} controls $score spaces" +
                    if (isTurn) ". ${player.displayName}'s turn" else ""
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OwnerBadge(player, size = 26.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = if (isTurn) "Choosing" else "Waiting",
                style = MaterialTheme.typography.bodySmall,
                color = if (isTurn) tone.accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = TABULAR),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (score == 1) "tile" else "tiles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Proportional bar: Player 1 | neutral | Player 2, with neutral count label. */
@Composable
fun TerritoryBar(player1: Int, player2: Int, neutral: Int, modifier: Modifier = Modifier) {
    val palette = LocalTerritoryPalette.current
    val total = (player1 + player2 + neutral).coerceAtLeast(1).toFloat()
    val p1 by animateFloatAsState(player1 / total, tween(260), label = "p1Share")
    val p2 by animateFloatAsState(player2 / total, tween(260), label = "p2Share")
    val neutralColor = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier.clearAndSetSemantics {
            contentDescription = "Player 1 $player1 tiles, Player 2 $player2 tiles, $neutral neutral remaining"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        ) {
            drawRect(neutralColor)
            val w1 = size.width * p1
            val w2 = size.width * p2
            drawRect(Brush.horizontalGradient(listOf(palette.playerOne.dark, palette.playerOne.light), 0f, w1.coerceAtLeast(1f)), size = size.copy(width = w1))
            drawRect(
                Brush.horizontalGradient(listOf(palette.playerTwo.light, palette.playerTwo.dark), size.width - w2, size.width),
                topLeft = androidx.compose.ui.geometry.Offset(size.width - w2, 0f),
                size = size.copy(width = w2)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "NEUTRAL  $neutral",
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = TABULAR),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorSelector(
    legalColors: Set<TerrainColor>,
    gains: Map<TerrainColor, Int>,
    showLegal: Boolean,
    inputEnabled: Boolean,
    onSelect: (TerrainColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TerrainColor.entries.forEach { terrain ->
            val legal = terrain in legalColors
            ColorChip(
                terrain = terrain,
                legal = legal,
                gain = gains[terrain] ?: 0,
                showLegal = showLegal,
                clickable = inputEnabled && (legal || !showLegal),
                onClick = { onSelect(terrain) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private val TerrainColor.shortLabel: String
    get() = when (this) {
        TerrainColor.TERRACOTTA -> "TERRA"
        TerrainColor.SAGE -> "SAGE"
        TerrainColor.SAND -> "SAND"
        TerrainColor.SLATE -> "SLATE"
        TerrainColor.PLUM -> "PLUM"
    }

@Composable
private fun ColorChip(
    terrain: TerrainColor,
    legal: Boolean,
    gain: Int,
    showLegal: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val palette = LocalTerritoryPalette.current
    val interaction = remember { MutableInteractionSource() }
    val dimmed = showLegal && !legal
    val alpha by animateFloatAsState(if (dimmed) 0.32f else 1f, tween(180), label = "chipAlpha")
    val shape = RoundedCornerShape(12.dp)
    val base = palette.terrainColor(terrain)
    val description = buildString {
        append("${terrain.displayName} color")
        if (showLegal) {
            if (legal) append(", legal move, gains $gain ${if (gain == 1) "tile" else "tiles"}")
            else append(", unavailable")
        }
    }
    Column(
        modifier
            .pressScale(interaction, pressed = 0.92f)
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable(interaction, ripple(color = base), enabled = clickable, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                if (clickable) onClick { onClick(); true } else disabled()
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(30.dp)) {
            val side = size.minDimension
            drawRoundRect(base, cornerRadius = CornerRadius(side * 0.22f))
            drawRoundRect(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.Black.copy(alpha = 0.14f))),
                cornerRadius = CornerRadius(side * 0.22f)
            )
            drawTerrainSymbol(terrain, center, side * 0.32f, symbolInk(base))
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = terrain.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (showLegal && legal) "+$gain" else " ",
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = TABULAR),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun GameControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    SecondaryButton(text = text, onClick = onClick, enabled = enabled, modifier = modifier, height = 42.dp)
}

@Composable
fun NoticePill(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

