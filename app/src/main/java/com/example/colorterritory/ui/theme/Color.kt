package com.example.colorterritory.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.colorterritory.data.BoardTheme
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner

// App chrome — dark (graphite)
val GraphiteBackground = Color(0xFF141517)
val GraphiteSurface = Color(0xFF1C1D20)
val GraphiteSurfaceHigh = Color(0xFF25262A)
val GraphiteOutline = Color(0xFF36373C)
val GraphiteOutlineSoft = Color(0xFF2A2B30)
val IvoryText = Color(0xFFECE6DC)
val IvoryMuted = Color(0xFF9C978E)
val Ivory = Color(0xFFE9E2D6)

// App chrome — light (warm paper)
val PaperBackground = Color(0xFFF2EEE7)
val PaperSurface = Color(0xFFFAF7F2)
val PaperSurfaceHigh = Color(0xFFE9E4DB)
val PaperOutline = Color(0xFFD3CDC2)
val PaperOutlineSoft = Color(0xFFE2DDD3)
val InkText = Color(0xFF1F2023)
val InkMuted = Color(0xFF6E6A63)
val Ink = Color(0xFF26272B)

@Immutable
data class PlayerTone(
    val light: Color,
    val dark: Color,
    val accent: Color
)

@Immutable
data class TerritoryPalette(
    val terrain: Map<TerrainColor, Color>,
    val playerOne: PlayerTone,
    val playerTwo: PlayerTone,
    val tray: Color,
    val trayBorder: Color
) {
    fun terrainColor(terrain: TerrainColor): Color = this.terrain.getValue(terrain)

    fun tone(player: Player): PlayerTone =
        if (player == Player.PLAYER_ONE) playerOne else playerTwo

    fun tone(owner: TerritoryOwner): PlayerTone? = when (owner) {
        TerritoryOwner.PLAYER_ONE -> playerOne
        TerritoryOwner.PLAYER_TWO -> playerTwo
        TerritoryOwner.NONE -> null
    }
}

private val Copper = PlayerTone(
    light = Color(0xFFE3A873),
    dark = Color(0xFFA2643A),
    accent = Color(0xFFD4945A)
)

private val SteelTeal = PlayerTone(
    light = Color(0xFFA4D0CF),
    dark = Color(0xFF4C8589),
    accent = Color(0xFF7DB5B5)
)

val ClassicPalette = TerritoryPalette(
    terrain = mapOf(
        TerrainColor.TERRACOTTA to Color(0xFFA65F4B),
        TerrainColor.SAGE to Color(0xFF8C9C7A),
        TerrainColor.SAND to Color(0xFFCDB68C),
        TerrainColor.SLATE to Color(0xFF5B7391),
        TerrainColor.PLUM to Color(0xFF7A5570)
    ),
    playerOne = Copper,
    playerTwo = SteelTeal,
    tray = Color(0xFF1F2023),
    trayBorder = Color(0xFF34353A)
)

val MidnightPalette = TerritoryPalette(
    terrain = mapOf(
        TerrainColor.TERRACOTTA to Color(0xFF8E5445),
        TerrainColor.SAGE to Color(0xFF6E8270),
        TerrainColor.SAND to Color(0xFFA9976F),
        TerrainColor.SLATE to Color(0xFF4C6180),
        TerrainColor.PLUM to Color(0xFF654862)
    ),
    playerOne = Copper,
    playerTwo = SteelTeal,
    tray = Color(0xFF10141B),
    trayBorder = Color(0xFF263041)
)

fun paletteFor(theme: BoardTheme): TerritoryPalette = when (theme) {
    BoardTheme.CLASSIC -> ClassicPalette
    BoardTheme.MIDNIGHT -> MidnightPalette
}

val TealAccent = Color(0xFF7DB5B5)
val TealDeep = Color(0xFF3F7478)
val ClayError = Color(0xFFC0705E)
