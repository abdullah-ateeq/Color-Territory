package com.example.colorterritory.game

/**
 * Fixed, tested terrain layout. Row 0 is the top of the board.
 *
 * The map is point-symmetric (a 180° rotation maps it onto itself), so both
 * corner starts see the same terrain. Colour counts are 12–14 each and no
 * single-colour region is larger than four tiles.
 *
 * T = Terracotta, G = Sage, S = Sand, B = Slate blue, P = Plum
 */
object BoardLayout {
    val ROWS: List<String> = listOf(
        "BGGSGTBS",
        "TGSSBGBP",
        "SPPPTTGP",
        "TTGPBBSP",
        "PSBBPGTT",
        "PGTTPPPS",
        "PBGBSSGT",
        "SBTGSGGB"
    )

    fun terrainAt(row: Int, col: Int): TerrainColor = when (ROWS[row][col]) {
        'T' -> TerrainColor.TERRACOTTA
        'G' -> TerrainColor.SAGE
        'S' -> TerrainColor.SAND
        'B' -> TerrainColor.SLATE
        'P' -> TerrainColor.PLUM
        else -> error("Unknown terrain code")
    }
}
