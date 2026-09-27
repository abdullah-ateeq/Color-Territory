package com.example.colorterritory.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerritoryOwner
import com.example.colorterritory.ui.theme.PlayerTone
import com.example.colorterritory.ui.theme.TerritoryPalette

/** Ink colour for a terrain symbol drawn on top of [base]. */
fun symbolInk(base: Color): Color =
    if (base.luminance() > 0.3f) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.40f)

/**
 * Accessibility symbol for each terrain: dot, line, diamond, cross, ring.
 * [radius] is the half-extent of the symbol's bounding box.
 */
fun DrawScope.drawTerrainSymbol(
    terrain: TerrainColor,
    center: Offset,
    radius: Float,
    color: Color
) {
    val stroke = (radius * 0.24f).coerceAtLeast(1f)
    when (terrain) {
        TerrainColor.TERRACOTTA -> drawCircle(color, radius * 0.34f, center)
        TerrainColor.SAGE -> drawLine(
            color,
            Offset(center.x - radius * 0.55f, center.y + radius * 0.55f),
            Offset(center.x + radius * 0.55f, center.y - radius * 0.55f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        TerrainColor.SAND -> {
            val r = radius * 0.5f
            val path = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y)
                lineTo(center.x, center.y + r)
                lineTo(center.x - r, center.y)
                close()
            }
            drawPath(path, color)
        }
        TerrainColor.SLATE -> {
            val r = radius * 0.5f
            drawLine(color, Offset(center.x - r, center.y), Offset(center.x + r, center.y), stroke, StrokeCap.Round)
            drawLine(color, Offset(center.x, center.y - r), Offset(center.x, center.y + r), stroke, StrokeCap.Round)
        }
        TerrainColor.PLUM -> drawCircle(color, radius * 0.42f, center, style = Stroke(stroke))
    }
}

/**
 * Player ownership mark: Player 1 is round, Player 2 is square, so owners are
 * never distinguished by colour alone.
 */
fun DrawScope.drawOwnerMark(
    owner: TerritoryOwner,
    center: Offset,
    radius: Float,
    fill: Color,
    ring: Color
) {
    when (owner) {
        TerritoryOwner.PLAYER_ONE -> {
            drawCircle(fill, radius, center)
            drawCircle(ring, radius, center, style = Stroke((radius * 0.22f).coerceAtLeast(1f)))
        }
        TerritoryOwner.PLAYER_TWO -> {
            val side = radius * 1.8f
            val topLeft = Offset(center.x - side / 2, center.y - side / 2)
            val corner = CornerRadius(side * 0.18f)
            drawRoundRect(fill, topLeft, Size(side, side), corner)
            drawRoundRect(ring, topLeft, Size(side, side), corner, style = Stroke((radius * 0.22f).coerceAtLeast(1f)))
        }
        TerritoryOwner.NONE -> Unit
    }
}

/** Metallic ownership fill used by tiles and player badges. */
fun DrawScope.drawMetal(tone: PlayerTone, topLeft: Offset, size: Size, corner: Float, alpha: Float = 1f) {
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(tone.light, tone.accent, tone.dark),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(corner),
        alpha = alpha
    )
}

/**
 * Draws one territory tile.
 *
 * @param ownership 0 = neutral, 1 = fully owned; intermediate values animate
 *                  the ownership fill growing from the centre.
 * @param owner the owner to draw when [ownership] > 0.
 * @param highlight 0..1 strength of the last-expansion outline.
 */
fun DrawScope.drawTerritoryTile(
    topLeft: Offset,
    side: Float,
    terrain: TerrainColor,
    owner: TerritoryOwner,
    ownership: Float,
    palette: TerritoryPalette,
    highlight: Float = 0f
) {
    val corner = side * 0.16f
    val size = Size(side, side)
    val base = palette.terrainColor(terrain)
    val center = Offset(topLeft.x + side / 2, topLeft.y + side / 2)

    drawRoundRect(base, topLeft, size, CornerRadius(corner))
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.14f)),
            startY = topLeft.y,
            endY = topLeft.y + side
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(corner)
    )

    val tone = palette.tone(owner)
    val p = if (tone == null) 0f else ownership.coerceIn(0f, 1f)
    if (p < 1f) {
        drawTerrainSymbol(terrain, center, side * 0.30f, symbolInk(base).copy(alpha = symbolInk(base).alpha * (1f - p)))
    }

    if (tone != null && p > 0f) {
        val inset = side * 0.5f * (1f - p)
        val fillSide = side - inset * 2
        val fillTopLeft = Offset(topLeft.x + inset, topLeft.y + inset)
        drawMetal(tone, fillTopLeft, Size(fillSide, fillSide), corner * p.coerceAtLeast(0.4f))
        val hairline = (side * 0.03f).coerceAtLeast(1f)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f * p),
            topLeft = Offset(fillTopLeft.x + hairline / 2, fillTopLeft.y + hairline / 2),
            size = Size(fillSide - hairline, fillSide - hairline),
            cornerRadius = CornerRadius(corner * p.coerceAtLeast(0.4f)),
            style = Stroke(hairline)
        )
        // Terrain-colour centre mark keeps the original terrain readable.
        drawOwnerMark(
            owner = owner,
            center = center,
            radius = side * 0.15f * p,
            fill = base,
            ring = Color.Black.copy(alpha = 0.28f * p)
        )
    }

    if (highlight > 0f) {
        val w = (side * 0.05f).coerceAtLeast(1.5f)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.75f * highlight),
            topLeft = Offset(topLeft.x + w / 2, topLeft.y + w / 2),
            size = Size(side - w, side - w),
            cornerRadius = CornerRadius(corner),
            style = Stroke(w)
        )
    }
}
