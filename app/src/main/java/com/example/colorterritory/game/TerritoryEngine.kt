package com.example.colorterritory.game

/**
 * Pure, UI-independent rules for Color Territory. Every function returns new
 * immutable values; nothing here touches Android APIs.
 */
object TerritoryEngine {
    const val SIZE = 8
    const val CELL_COUNT = SIZE * SIZE

    val PLAYER_ONE_START = GridPosition(SIZE - 1, 0)
    val PLAYER_TWO_START = GridPosition(0, SIZE - 1)

    fun createInitialBoard(): Board =
        List(SIZE) { row ->
            List(SIZE) { col ->
                val position = GridPosition(row, col)
                TerritoryCell(
                    terrainColor = BoardLayout.terrainAt(row, col),
                    owner = when (position) {
                        PLAYER_ONE_START -> TerritoryOwner.PLAYER_ONE
                        PLAYER_TWO_START -> TerritoryOwner.PLAYER_TWO
                        else -> TerritoryOwner.NONE
                    }
                )
            }
        }

    fun newGame(board: Board = createInitialBoard()): ColorTerritoryState {
        val (p1, p2) = scores(board)
        return ColorTerritoryState(
            board = board,
            currentPlayer = Player.PLAYER_ONE,
            legalColors = getLegalColors(board, Player.PLAYER_ONE),
            player1Score = p1,
            player2Score = p2
        )
    }

    fun getNeighbors(position: GridPosition): List<GridPosition> {
        val result = ArrayList<GridPosition>(4)
        if (position.row > 0) result += GridPosition(position.row - 1, position.col)
        if (position.row < SIZE - 1) result += GridPosition(position.row + 1, position.col)
        if (position.col > 0) result += GridPosition(position.row, position.col - 1)
        if (position.col < SIZE - 1) result += GridPosition(position.row, position.col + 1)
        return result
    }

    private operator fun Board.get(position: GridPosition): TerritoryCell =
        this[position.row][position.col]

    /**
     * Cells claimed by [player] choosing [terrainColor], grouped by BFS distance
     * from the existing territory (layer 0 touches the territory directly).
     */
    fun getExpansionLayers(
        board: Board,
        player: Player,
        terrainColor: TerrainColor
    ): List<Set<GridPosition>> {
        val owner = player.owner
        val visited = HashSet<GridPosition>()
        var frontier = LinkedHashSet<GridPosition>()

        fun claimable(position: GridPosition): Boolean {
            val cell = board[position]
            return cell.owner == TerritoryOwner.NONE && cell.terrainColor == terrainColor
        }

        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                if (board[row][col].owner != owner) continue
                for (neighbor in getNeighbors(GridPosition(row, col))) {
                    if (neighbor !in visited && claimable(neighbor)) {
                        visited += neighbor
                        frontier += neighbor
                    }
                }
            }
        }

        val layers = ArrayList<Set<GridPosition>>()
        while (frontier.isNotEmpty()) {
            layers += frontier
            val next = LinkedHashSet<GridPosition>()
            for (position in frontier) {
                for (neighbor in getNeighbors(position)) {
                    if (neighbor !in visited && claimable(neighbor)) {
                        visited += neighbor
                        next += neighbor
                    }
                }
            }
            frontier = next
        }
        return layers
    }

    fun getExpansionCells(
        board: Board,
        player: Player,
        terrainColor: TerrainColor
    ): Set<GridPosition> = getExpansionLayers(board, player, terrainColor).flatten().toSet()

    fun getLegalColors(board: Board, player: Player): Set<TerrainColor> =
        TerrainColor.entries.filterTo(LinkedHashSet()) {
            getExpansionCells(board, player, it).isNotEmpty()
        }

    /** Gain for every legal colour; used for the optional "+N" preview. */
    fun getExpansionPreview(board: Board, player: Player): Map<TerrainColor, Int> =
        TerrainColor.entries.associateWith { getExpansionCells(board, player, it).size }
            .filterValues { it > 0 }

    fun scores(board: Board): Pair<Int, Int> {
        var p1 = 0
        var p2 = 0
        for (row in board) for (cell in row) {
            when (cell.owner) {
                TerritoryOwner.PLAYER_ONE -> p1++
                TerritoryOwner.PLAYER_TWO -> p2++
                TerritoryOwner.NONE -> Unit
            }
        }
        return p1 to p2
    }

    fun withOwner(board: Board, cells: Set<GridPosition>, owner: TerritoryOwner): Board {
        if (cells.isEmpty()) return board
        return board.mapIndexed { row, cellsInRow ->
            cellsInRow.mapIndexed { col, cell ->
                if (GridPosition(row, col) in cells && cell.owner == TerritoryOwner.NONE) {
                    cell.copy(owner = owner)
                } else {
                    cell
                }
            }
        }
    }

    /**
     * Applies a colour choice for the current player. Returns null when the move
     * is not allowed (game over, animating, or the colour gains nothing).
     *
     * The returned state has already switched to the next player (unless the
     * game ended) with fresh legal colours; if those are empty the caller should
     * follow up with [applyPass].
     */
    fun applyColor(state: ColorTerritoryState, terrainColor: TerrainColor): ColorTerritoryState? {
        if (state.isGameOver || state.isAnimating) return null
        val player = state.currentPlayer
        val claimed = getExpansionCells(state.board, player, terrainColor)
        if (claimed.isEmpty()) return null

        val board = withOwner(state.board, claimed, player.owner)
        val (p1, p2) = scores(board)
        val afterMove = state.copy(
            board = board,
            lastExpansion = claimed,
            player1Score = p1,
            player2Score = p2,
            moveCount = state.moveCount + 1,
            consecutivePasses = 0
        )
        if (p1 + p2 == CELL_COUNT) return finishGame(afterMove)

        val next = player.opponent
        return afterMove.copy(
            currentPlayer = next,
            legalColors = getLegalColors(board, next)
        )
    }

    /** Automatic pass for a player with no legal colour. */
    fun applyPass(state: ColorTerritoryState): ColorTerritoryState {
        if (state.isGameOver) return state
        val passes = state.consecutivePasses + 1
        val passed = state.copy(consecutivePasses = passes)
        if (passes >= 2) return finishGame(passed)
        val next = state.currentPlayer.opponent
        return passed.copy(
            currentPlayer = next,
            legalColors = getLegalColors(state.board, next)
        )
    }

    fun finishGame(state: ColorTerritoryState): ColorTerritoryState {
        val (p1, p2) = scores(state.board)
        return state.copy(
            player1Score = p1,
            player2Score = p2,
            legalColors = emptySet(),
            winner = when {
                p1 > p2 -> Player.PLAYER_ONE
                p2 > p1 -> Player.PLAYER_TWO
                else -> null
            },
            isDraw = p1 == p2,
            isAnimating = false
        )
    }
}
