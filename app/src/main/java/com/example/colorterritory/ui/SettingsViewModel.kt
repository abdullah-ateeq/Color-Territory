package com.example.colorterritory.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorterritory.data.AppSettings
import com.example.colorterritory.data.BoardTheme
import com.example.colorterritory.data.GameStats
import com.example.colorterritory.data.SettingsRepository
import com.example.colorterritory.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val stats: StateFlow<GameStats> =
        repository.stats.stateIn(viewModelScope, SharingStarted.Eagerly, GameStats())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSound(enabled: Boolean) = launch { repository.setSoundEnabled(enabled) }
    fun setHaptics(enabled: Boolean) = launch { repository.setHapticsEnabled(enabled) }
    fun setShowLegalColors(enabled: Boolean) = launch { repository.setShowLegalColors(enabled) }
    fun setShowLastExpansion(enabled: Boolean) = launch { repository.setShowLastExpansion(enabled) }
    fun resetStats() = launch { repository.resetStats() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
