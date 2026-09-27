package com.example.colorterritory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.colorterritory.data.AppSettings
import com.example.colorterritory.data.BoardTheme
import com.example.colorterritory.data.GameStats
import com.example.colorterritory.data.ThemeMode
import com.example.colorterritory.ui.SettingsViewModel
import com.example.colorterritory.ui.components.CenteredColumn
import com.example.colorterritory.ui.components.ScreenScaffold
import com.example.colorterritory.ui.components.SecondaryButton
import com.example.colorterritory.ui.components.SectionLabel
import com.example.colorterritory.ui.components.SegmentedSelector
import com.example.colorterritory.ui.theme.LocalTerritoryPalette

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStats,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold(title = "Settings", onBack = onBack) {
        CenteredColumn(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            SectionLabel("Appearance")
            Spacer(Modifier.height(12.dp))
            Text("Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = ThemeMode.entries,
                selected = settings.themeMode,
                label = { it.label },
                onSelect = viewModel::setThemeMode,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text("Board theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = BoardTheme.entries,
                selected = settings.boardTheme,
                label = { it.label },
                onSelect = viewModel::setBoardTheme,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))
            SectionLabel("Gameplay")
            ToggleRow("Show legal colors", "Dim unavailable colors and preview gains", settings.showLegalColors, viewModel::setShowLegalColors)
            ToggleRow("Show last expansion", "Outline tiles claimed on the latest turn", settings.showLastExpansion, viewModel::setShowLastExpansion)

            Spacer(Modifier.height(20.dp))
            SectionLabel("Feedback")
            ToggleRow("Sound effects", null, settings.soundEnabled, viewModel::setSound)
            ToggleRow("Haptic feedback", null, settings.hapticsEnabled, viewModel::setHaptics)

            Spacer(Modifier.height(20.dp))
            SectionLabel("Statistics")
            Spacer(Modifier.height(8.dp))
            StatLine("Games played", stats.gamesPlayed)
            StatLine("Player 1 wins", stats.player1Wins)
            StatLine("Player 2 wins", stats.player2Wins)
            StatLine("Draws", stats.draws)
            StatLine("Largest territory", stats.largestTerritory)
            StatLine("Best winning margin", stats.bestWinningMargin)
            Spacer(Modifier.height(16.dp))
            SecondaryButton("RESET STATISTICS", { confirmReset = true }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset statistics?", style = MaterialTheme.typography.titleMedium) },
            text = { Text("All recorded games, wins and records will be cleared.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    viewModel.resetStats()
                }) { Text("RESET", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("CANCEL", style = MaterialTheme.typography.labelLarge) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalTerritoryPalette.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.playerOne.light,
                    checkedTrackColor = palette.playerOne.dark,
                    checkedBorderColor = palette.playerOne.dark,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun StatLine(label: String, value: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
