package com.example.colorterritory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.ui.components.CenteredColumn
import com.example.colorterritory.ui.components.ScreenScaffold
import com.example.colorterritory.ui.components.SectionLabel
import androidx.compose.foundation.Canvas
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.components.drawTerritoryTile
import com.example.colorterritory.ui.theme.LocalTerritoryPalette
import androidx.compose.ui.geometry.Offset

private val Features = listOf(
    "Local 2 Player",
    "8×8 Color Grid",
    "Flood Expansion",
    "Territory Strategy",
    "Offline Play"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val palette = LocalTerritoryPalette.current
    ScreenScaffold(title = "About", onBack = onBack) {
        CenteredColumn(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Color Territory", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Text(
                "A polished local two-player strategy game where choosing the right colors expands your territory across a shared grid.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(28.dp))
            SectionLabel("Features")
            Spacer(Modifier.height(8.dp))
            Features.forEachIndexed { index, feature ->
                Column {
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val terrain = TerrainColor.entries[index % TerrainColor.entries.size]
                        Canvas(Modifier.size(14.dp)) {
                            drawTerritoryTile(Offset.Zero, size.minDimension, terrain, TerritoryOwner.NONE, 0f, palette)
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(feature, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
