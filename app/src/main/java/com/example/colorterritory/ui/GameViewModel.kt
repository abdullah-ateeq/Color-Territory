package com.example.colorterritory.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorterritory.audio.Sfx
import com.example.colorterritory.audio.SoundEngine
import com.example.colorterritory.data.AppSettings
import com.example.colorterritory.data.SettingsRepository
import com.example.colorterritory.game.ColorTerritoryState
import com.example.colorterritory.game.GridPosition
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val game: ColorTerritoryState = TerritoryEngine.newGame(),
    /** Set while the "no expansion available" notice is shown for this player. */
    val passNotice: Player? = null,
    /** Set briefly when an unavailable colour is tapped with legal-colour hints off. */
    val rejectedColor: TerrainColor? = null,
    /** True while an expansion animation or automatic pass is being processed. */
    val isBusy: Boolean = false,
    val canUndo: Boolean = false,
    val matchDurationMs: Long = 0L,
    val gameId: Int = 0
) {
    val inputEnabled: Boolean get() = !isBusy && !game.isGameOver && !game.isAnimating
    val hasMatchInProgress: Boolean get() = game.moveCount > 0 && !game.isGameOver
}

sealed interface GameEvent {
    data object ColorSelected : GameEvent
    data class Expanded(val count: Int) : GameEvent
    data object Rejected : GameEvent
    data object Passed : GameEvent
    data class Finished(val winner: Player?) : GameEvent
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    @Volatile
    private var sound: SoundEngine? = null

    private val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private val undoStack = ArrayDeque<ColorTerritoryState>()
    private var turnJob: Job? = null
    private var noticeJob: Job? = null
    private var matchStartedAt = SystemClock.elapsedRealtime()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            sound = runCatching { SoundEngine(application) }.getOrNull()
        }
    }

    fun newGame() {
        turnJob?.cancel()
        noticeJob?.cancel()
        undoStack.clear()
        matchStartedAt = SystemClock.elapsedRealtime()
        _uiState.update { GameUiState(game = TerritoryEngine.newGame(), gameId = it.gameId + 1) }
    }

    fun selectColor(terrainColor: TerrainColor) {
        val ui = _uiState.value
        val state = ui.game
        if (!ui.inputEnabled) return

        if (terrainColor !in state.legalColors) {
            reject(terrainColor)
            return
        }

        val player = state.currentPlayer
        val layers = TerritoryEngine.getExpansionLayers(state.board, player, terrainColor)
        val result = TerritoryEngine.applyColor(state, terrainColor) ?: return

        undoStack.addLast(state)
        _uiState.update { it.copy(isBusy = true, canUndo = false, rejectedColor = null) }

        turnJob = viewModelScope.launch {
            play(Sfx.SELECT)
            _events.emit(GameEvent.ColorSelected)

            // Claim cells outward from the territory, one BFS layer at a time.
            var board = state.board
            val claimed = HashSet<GridPosition>()
            layers.forEachIndexed { index, layer ->
                board = TerritoryEngine.withOwner(board, layer, player.owner)
                claimed += layer
                val (p1, p2) = TerritoryEngine.scores(board)
                _uiState.update {
                    it.copy(
                        game = state.copy(
                            board = board,
                            lastExpansion = claimed.toSet(),
                            player1Score = p1,
                            player2Score = p2,
                            legalColors = emptySet(),
                            isAnimating = true
                        )
                    )
                }
                if (index == 0) {
                    play(Sfx.EXPAND)
                    _events.emit(GameEvent.Expanded(result.lastExpansion.size))
                }
                delay(LAYER_DELAY_MS)
            }
            delay(SETTLE_DELAY_MS)

            _uiState.update { it.copy(game = result) }
            if (result.isGameOver) {
                onGameOver(result)
            } else {
                resolveAutomaticPasses()
            }
            _uiState.update { it.copy(isBusy = false, canUndo = undoStack.isNotEmpty() && !it.game.isGameOver) }
        }
    }

    fun undo() {
        val ui = _uiState.value
        if (ui.isBusy || ui.game.isGameOver || undoStack.isEmpty()) return
        val previous = undoStack.removeLast().copy(isAnimating = false)
        _uiState.update {
            it.copy(game = previous, canUndo = undoStack.isNotEmpty(), passNotice = null, rejectedColor = null)
        }
    }

    private suspend fun resolveAutomaticPasses() {
        while (true) {
            val state = _uiState.value.game
            if (state.isGameOver || state.legalColors.isNotEmpty()) return
            _uiState.update { it.copy(passNotice = state.currentPlayer) }
            play(Sfx.PASS)
            _events.emit(GameEvent.Passed)
            delay(PASS_NOTICE_MS)
            val next = TerritoryEngine.applyPass(state)
            _uiState.update { it.copy(game = next, passNotice = null) }
            if (next.isGameOver) {
                onGameOver(next)
                return
            }
        }
    }

    private suspend fun onGameOver(state: ColorTerritoryState) {
        undoStack.clear()
        val duration = SystemClock.elapsedRealtime() - matchStartedAt
        _uiState.update { it.copy(matchDurationMs = duration, canUndo = false) }
        play(if (state.winner != null) Sfx.WIN else Sfx.COMPLETE)
        _events.emit(GameEvent.Finished(state.winner))
        repository.recordGame(state.winner, state.player1Score, state.player2Score)
    }

    private fun reject(terrainColor: TerrainColor) {
        _events.tryEmit(GameEvent.Rejected)
        noticeJob?.cancel()
        _uiState.update { it.copy(rejectedColor = terrainColor) }
        noticeJob = viewModelScope.launch {
            delay(REJECT_NOTICE_MS)
            _uiState.update { it.copy(rejectedColor = null) }
        }
    }

    private fun play(sfx: Sfx) {
        if (settings.value.soundEnabled) sound?.play(sfx)
    }

    override fun onCleared() {
        sound?.release()
        super.onCleared()
    }

    private companion object {
        const val LAYER_DELAY_MS = 90L
        const val SETTLE_DELAY_MS = 140L
        const val PASS_NOTICE_MS = 1300L
        const val REJECT_NOTICE_MS = 1100L
    }
}
