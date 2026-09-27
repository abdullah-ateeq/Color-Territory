package com.example.colorterritory.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.colorterritory.data.AppSettings
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerritoryEngine
import com.example.colorterritory.ui.GameEvent
import com.example.colorterritory.ui.GameUiState
import com.example.colorterritory.ui.GameViewModel
import com.example.colorterritory.ui.components.ColorSelector
import com.example.colorterritory.ui.components.GameControlButton
import com.example.colorterritory.ui.components.NoticePill
import com.example.colorterritory.ui.components.PlayerPanel
import com.example.colorterritory.ui.components.TerritoryBar
import com.example.colorterritory.ui.components.TerritoryBoard
import com.example.colorterritory.ui.theme.LocalTerritoryPalette
import kotlinx.coroutines.delay

private const val LARGE_EXPANSION = 5

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    onGameFinished: () -> Unit,
    onHome: () -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val currentSettings by rememberUpdatedState(settings)
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onHome)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (currentSettings.hapticsEnabled) performHaptic(view, event)
        }
    }

    LaunchedEffect(ui.gameId, ui.game.isGameOver) {
        if (ui.game.isGameOver) {
            delay(1400)
            onGameFinished()
        }
    }

    val gains = remember(ui.game.board, ui.game.currentPlayer) {
        TerritoryEngine.getExpansionPreview(ui.game.board, ui.game.currentPlayer)
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val landscape = maxWidth > maxHeight && maxWidth >= 560.dp
        val board: @Composable (Modifier) -> Unit = { modifier ->
            BoardWithOverlay(ui, settings, modifier)
        }
        val controls: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameControlButton("UNDO", viewModel::undo, enabled = ui.canUndo && ui.inputEnabled, modifier = Modifier.weight(1f))
                GameControlButton("RESTART", { showRestartDialog = true }, enabled = !ui.game.isGameOver, modifier = Modifier.weight(1f))
                GameControlButton("HOME", onHome, enabled = true, modifier = Modifier.weight(1f))
            }
        }
        val selector: @Composable () -> Unit = {
            ColorSelector(
                legalColors = ui.game.legalColors,
                gains = gains,
                showLegal = settings.showLegalColors,
                inputEnabled = ui.inputEnabled,
                onSelect = viewModel::selectColor
            )
        }

        if (landscape) {
            Row(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    board(Modifier.fillMaxSize())
                }
                Column(
                    Modifier
                        .widthIn(min = 300.dp, max = 400.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    PlayerPanel(Player.PLAYER_TWO, ui.game.player2Score, isTurnOf(ui, Player.PLAYER_TWO))
                    TurnHeader(ui)
                    TerritoryBar(ui.game.player1Score, ui.game.player2Score, ui.game.neutralCount)
                    selector()
                    PlayerPanel(Player.PLAYER_ONE, ui.game.player1Score, isTurnOf(ui, Player.PLAYER_ONE))
                    controls()
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayerPanel(Player.PLAYER_TWO, ui.game.player2Score, isTurnOf(ui, Player.PLAYER_TWO))
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        board(Modifier.fillMaxSize())
                    }
                    PlayerPanel(Player.PLAYER_ONE, ui.game.player1Score, isTurnOf(ui, Player.PLAYER_ONE))
                    TurnHeader(ui)
                    TerritoryBar(ui.game.player1Score, ui.game.player2Score, ui.game.neutralCount)
                    selector()
                    controls()
                }
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart this match?", style = MaterialTheme.typography.titleMedium) },
            text = { Text("The current board and scores will be cleared.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    viewModel.newGame()
                }) { Text("RESTART", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

private fun isTurnOf(ui: GameUiState, player: Player) =
    !ui.game.isGameOver && ui.game.currentPlayer == player

@Composable
private fun BoardWithOverlay(ui: GameUiState, settings: AppSettings, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        TerritoryBoard(
            board = ui.game.board,
            lastExpansion = ui.game.lastExpansion,
            showLastExpansion = settings.showLastExpansion
        )
        AnimatedVisibility(
            visible = ui.passNotice != null,
            enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.94f),
            exit = fadeOut(tween(160))
        ) {
            NoticePill(
                title = "NO EXPANSION AVAILABLE",
                subtitle = "${(ui.passNotice ?: Player.PLAYER_ONE).displayName} passes"
            )
        }
        AnimatedVisibility(
            visible = ui.game.isGameOver,
            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.94f),
            exit = fadeOut(tween(120))
        ) {
            NoticePill(
                title = "MATCH COMPLETE",
                subtitle = when {
                    ui.game.isDraw -> "Draw"
                    else -> "${ui.game.winner?.displayName} wins"
                }
            )
        }
    }
}

@Composable
private fun TurnHeader(ui: GameUiState) {
    val palette = LocalTerritoryPalette.current
    val game = ui.game
    val accent = palette.tone(game.currentPlayer).accent
    val title: String
    val subtitle: String
    when {
        game.isGameOver -> {
            title = "MATCH COMPLETE"
            subtitle = if (game.isDraw) "Territory is evenly split" else "${game.winner?.displayName} controls more ground"
        }
        ui.passNotice != null -> {
            title = "NO EXPANSION AVAILABLE"
            subtitle = "${ui.passNotice.displayName} passes"
        }
        ui.rejectedColor != null -> {
            title = if (game.currentPlayer == Player.PLAYER_ONE) "PLAYER 1'S TURN" else "PLAYER 2'S TURN"
            subtitle = "${ui.rejectedColor.displayName} cannot expand your territory"
        }
        else -> {
            title = if (game.currentPlayer == Player.PLAYER_ONE) "PLAYER 1'S TURN" else "PLAYER 2'S TURN"
            subtitle = if (game.isAnimating) "Expanding…" else "Choose a color"
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (game.isGameOver || ui.passNotice != null) MaterialTheme.colorScheme.onBackground else accent,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun performHaptic(view: View, event: GameEvent) {
    val constant = when (event) {
        GameEvent.ColorSelected -> HapticFeedbackConstants.CLOCK_TICK
        is GameEvent.Expanded ->
            if (event.count >= LARGE_EXPANSION) HapticFeedbackConstants.VIRTUAL_KEY
            else HapticFeedbackConstants.KEYBOARD_TAP
        GameEvent.Rejected, GameEvent.Passed ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.CLOCK_TICK
        is GameEvent.Finished ->
            if (event.winner == null) HapticFeedbackConstants.VIRTUAL_KEY
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
    }
    view.performHapticFeedback(constant)
}

