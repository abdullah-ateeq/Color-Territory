package com.example.colorterritory.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.colorterritory.game.Board
import com.example.colorterritory.game.GridPosition
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.theme.LocalTerritoryPalette

@Composable
fun TerritoryBoard(
    board: Board,
    modifier: Modifier = Modifier,
    lastExpansion: Set<GridPosition> = emptySet(),
    showLastExpansion: Boolean = true,
    gap: Dp = 3.dp,
    animate: Boolean = true
) {
    val palette = LocalTerritoryPalette.current
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(palette.tray)
            .border(1.dp, palette.trayBorder, shape)
            .padding(gap * 2)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            board.forEachIndexed { row, cells ->
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    cells.forEachIndexed { col, cell ->
                        BoardTile(
                            row = row,
                            col = col,
                            terrain = cell.terrainColor,
                            owner = cell.owner,
                            highlighted = showLastExpansion && GridPosition(row, col) in lastExpansion,
                            animate = animate,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardTile(
    row: Int,
    col: Int,
    terrain: TerrainColor,
    owner: TerritoryOwner,
    highlighted: Boolean,
    animate: Boolean,
    modifier: Modifier
) {
    val palette = LocalTerritoryPalette.current
    val ownership by animateFloatAsState(
        targetValue = if (owner != TerritoryOwner.NONE) 1f else 0f,
        animationSpec = if (animate) tween(200, easing = FastOutSlowInEasing) else tween(0),
        label = "ownership"
    )
    val highlight by animateFloatAsState(
        targetValue = if (highlighted) 1f else 0f,
        animationSpec = tween(if (animate) 260 else 0),
        label = "highlight"
    )
    // Keep drawing the previous owner while an undo fades a tile back to neutral.
    val lastOwner = remember { mutableStateOf(owner) }
    SideEffect { if (owner != TerritoryOwner.NONE) lastOwner.value = owner }
    val drawnOwner = if (owner != TerritoryOwner.NONE) owner else lastOwner.value

    val description = buildString {
        append("Row ${row + 1}, column ${col + 1}. ")
        when (owner) {
            TerritoryOwner.PLAYER_ONE -> append("Player 1 owned tile, ${terrain.displayName} terrain")
            TerritoryOwner.PLAYER_TWO -> append("Player 2 owned tile, ${terrain.displayName} terrain")
            TerritoryOwner.NONE -> append("${terrain.displayName} terrain, neutral")
        }
    }

    Box(
        modifier
            .semantics { contentDescription = description }
            .drawBehind {
                drawTerritoryTile(
                    topLeft = Offset.Zero,
                    side = size.minDimension,
                    terrain = terrain,
                    owner = drawnOwner,
                    ownership = ownership,
                    palette = palette,
                    highlight = highlight
                )
            }
    )
}
