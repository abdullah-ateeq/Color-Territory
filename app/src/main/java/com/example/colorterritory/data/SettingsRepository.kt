package com.example.colorterritory.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.colorterritory.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

enum class BoardTheme(val label: String) { CLASSIC("Classic"), MIDNIGHT("Midnight") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLegalColors: Boolean = true,
    val showLastExpansion: Boolean = true
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val draws: Int = 0,
    val largestTerritory: Int = 0,
    val bestWinningMargin: Int = 0
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "color_territory")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SHOW_LEGAL = booleanPreferencesKey("show_legal_colors")
        val SHOW_LAST = booleanPreferencesKey("show_last_expansion")

        val GAMES = intPreferencesKey("stats_games")
        val P1_WINS = intPreferencesKey("stats_p1_wins")
        val P2_WINS = intPreferencesKey("stats_p2_wins")
        val DRAWS = intPreferencesKey("stats_draws")
        val LARGEST = intPreferencesKey("stats_largest_territory")
        val BEST_MARGIN = intPreferencesKey("stats_best_margin")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.SYSTEM,
            boardTheme = p[Keys.BOARD_THEME]?.let { name -> BoardTheme.entries.firstOrNull { it.name == name } }
                ?: BoardTheme.CLASSIC,
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLegalColors = p[Keys.SHOW_LEGAL] ?: true,
            showLastExpansion = p[Keys.SHOW_LAST] ?: true
        )
    }

    val stats: Flow<GameStats> = store.data.map { p ->
        GameStats(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            draws = p[Keys.DRAWS] ?: 0,
            largestTerritory = p[Keys.LARGEST] ?: 0,
            bestWinningMargin = p[Keys.BEST_MARGIN] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD_THEME] = theme.name }
    suspend fun setSoundEnabled(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLegalColors(enabled: Boolean) = store.edit { it[Keys.SHOW_LEGAL] = enabled }
    suspend fun setShowLastExpansion(enabled: Boolean) = store.edit { it[Keys.SHOW_LAST] = enabled }

    suspend fun recordGame(winner: Player?, player1Score: Int, player2Score: Int) {
        store.edit { p ->
            p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
            when (winner) {
                Player.PLAYER_ONE -> p[Keys.P1_WINS] = (p[Keys.P1_WINS] ?: 0) + 1
                Player.PLAYER_TWO -> p[Keys.P2_WINS] = (p[Keys.P2_WINS] ?: 0) + 1
                null -> p[Keys.DRAWS] = (p[Keys.DRAWS] ?: 0) + 1
            }
            p[Keys.LARGEST] = maxOf(p[Keys.LARGEST] ?: 0, player1Score, player2Score)
            if (winner != null) {
                val margin = kotlin.math.abs(player1Score - player2Score)
                p[Keys.BEST_MARGIN] = maxOf(p[Keys.BEST_MARGIN] ?: 0, margin)
            }
        }
    }

    suspend fun resetStats() {
        store.edit { p ->
            p.remove(Keys.GAMES)
            p.remove(Keys.P1_WINS)
            p.remove(Keys.P2_WINS)
            p.remove(Keys.DRAWS)
            p.remove(Keys.LARGEST)
            p.remove(Keys.BEST_MARGIN)
        }
    }
}
