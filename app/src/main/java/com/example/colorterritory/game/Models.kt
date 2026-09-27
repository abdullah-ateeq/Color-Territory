package com.example.colorterritory.game

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO;

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    val owner: TerritoryOwner
        get() = if (this == PLAYER_ONE) TerritoryOwner.PLAYER_ONE else TerritoryOwner.PLAYER_TWO

    val displayName: String
        get() = if (this == PLAYER_ONE) "Player 1" else "Player 2"
}

enum class TerritoryOwner {
    NONE,
    PLAYER_ONE,
    PLAYER_TWO
}

enum class TerrainColor(val displayName: String, val symbolName: String) {
    TERRACOTTA("Terracotta", "dot"),
    SAGE("Sage", "line"),
    SAND("Sand", "diamond"),
    SLATE("Slate", "cross"),
    PLUM("Plum", "ring")
}

data class GridPosition(
    val row: Int,
    val col: Int
)

data class TerritoryCell(
    val terrainColor: TerrainColor,
    val owner: TerritoryOwner = TerritoryOwner.NONE
)

typealias Board = List<List<TerritoryCell>>

data class ColorTerritoryState(
    val board: Board,
    val currentPlayer: Player = Player.PLAYER_ONE,
    val legalColors: Set<TerrainColor> = emptySet(),
    val lastExpansion: Set<GridPosition> = emptySet(),
    val player1Score: Int = 1,
    val player2Score: Int = 1,
    val consecutivePasses: Int = 0,
    val moveCount: Int = 0,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val isAnimating: Boolean = false
) {
    val isGameOver: Boolean
        get() = winner != null || isDraw

    val neutralCount: Int
        get() = TerritoryEngine.CELL_COUNT - player1Score - player2Score

    fun scoreOf(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1Score else player2Score
}
