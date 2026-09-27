package com.example.colorterritory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.ColorTerritoryState
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerritoryEngine
import com.example.colorterritory.ui.components.OwnerBadge
import com.example.colorterritory.ui.components.PrimaryButton
import com.example.colorterritory.ui.components.ScreenScaffold
import com.example.colorterritory.ui.components.SecondaryButton
import com.example.colorterritory.ui.components.TerritoryBoard
import com.example.colorterritory.ui.theme.LocalTerritoryPalette
import kotlin.math.abs

@Composable
fun ResultScreen(
    game: ColorTerritoryState,
    durationMs: Long,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    BackHandler(onBack = onHome)
    val palette = LocalTerritoryPalette.current
    val headline = when {
        game.isDraw -> "DRAW"
        game.winner == Player.PLAYER_ONE -> "PLAYER 1 WINS"
        else -> "PLAYER 2 WINS"
    }
    val headlineColor = when (game.winner) {
        null -> MaterialTheme.colorScheme.onBackground
        else -> palette.tone(game.winner).accent
    }
    val owned = game.player1Score + game.player2Score
    val coverage = owned * 100 / TerritoryEngine.CELL_COUNT

    ScreenScaffold(title = null, onBack = null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("MATCH RESULT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text(
                    headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = headlineColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreCard(Player.PLAYER_ONE, game.player1Score, game.winner == Player.PLAYER_ONE, Modifier.weight(1f))
                    ScoreCard(Player.PLAYER_TWO, game.player2Score, game.winner == Player.PLAYER_TWO, Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                TerritoryBoard(
                    board = game.board,
                    showLastExpansion = false,
                    animate = false,
                    gap = 2.dp,
                    modifier = Modifier.fillMaxWidth(0.62f)
                )
                Spacer(Modifier.height(20.dp))
                DetailRow("Territory difference", abs(game.player1Score - game.player2Score).toString())
                DetailRow("Turns played", game.moveCount.toString())
                DetailRow("Board coverage", "$coverage%")
                DetailRow("Match duration", formatDuration(durationMs))
                Spacer(Modifier.height(28.dp))
                PrimaryButton("PLAY AGAIN", onPlayAgain, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                SecondaryButton("HOME", onHome, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ScoreCard(player: Player, score: Int, isWinner: Boolean, modifier: Modifier) {
    val palette = LocalTerritoryPalette.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isWinner) palette.tone(player).accent.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant,
                shape
            )
            .clearAndSetSemantics { contentDescription = "${player.displayName}, $score tiles" }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OwnerBadge(player, size = 22.dp)
        Spacer(Modifier.height(10.dp))
        Text(
            if (player == Player.PLAYER_ONE) "PLAYER 1" else "PLAYER 2",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(score.toString(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        Text("TILES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clearAndSetSemantics { contentDescription = "$label: $value" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
