package com.example.colorterritory

import com.example.colorterritory.game.Board
import com.example.colorterritory.game.BoardLayout
import com.example.colorterritory.game.ColorTerritoryState
import com.example.colorterritory.game.GridPosition
import com.example.colorterritory.game.Player
import com.example.colorterritory.game.TerrainColor
import com.example.colorterritory.game.TerrainColor.PLUM
import com.example.colorterritory.game.TerrainColor.SAGE
import com.example.colorterritory.game.TerrainColor.SAND
import com.example.colorterritory.game.TerrainColor.SLATE
import com.example.colorterritory.game.TerrainColor.TERRACOTTA
import com.example.colorterritory.game.TerritoryCell
import com.example.colorterritory.game.TerritoryEngine
import com.example.colorterritory.game.TerritoryOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerritoryEngineTest {

    /**
     * Builds a board from terrain rows (T/G/S/B/P) and owner rows
     * ('1' = Player 1, '2' = Player 2, '.' = neutral).
     */
    private fun board(terrain: List<String>, owners: List<String>): Board =
        terrain.mapIndexed { r, row ->
            row.mapIndexed { c, code ->
                TerritoryCell(
                    terrainColor = when (code) {
                        'T' -> TERRACOTTA
                        'G' -> SAGE
                        'S' -> SAND
                        'B' -> SLATE
                        else -> PLUM
                    },
                    owner = when (owners[r][c]) {
                        '1' -> TerritoryOwner.PLAYER_ONE
                        '2' -> TerritoryOwner.PLAYER_TWO
                        else -> TerritoryOwner.NONE
                    }
                )
            }
        }

    private val emptyOwners = List(8) { "........" }

    private fun owners(vararg overrides: Pair<GridPosition, Char>): List<String> {
        val rows = emptyOwners.map { it.toCharArray() }
        overrides.forEach { (p, ch) -> rows[p.row][p.col] = ch }
        return rows.map { String(it) }
    }

    private val p1Start = TerritoryEngine.PLAYER_ONE_START
    private val p2Start = TerritoryEngine.PLAYER_TWO_START

    /** Alternating columns of Sand/Plum, so nothing touches diagonally by accident. */
    private val striped = List(8) { "SPSPSPSP" }

    @Test
    fun initialOwnership() {
        val state = TerritoryEngine.newGame()
        assertEquals(GridPosition(7, 0), p1Start)
        assertEquals(GridPosition(0, 7), p2Start)
        assertEquals(TerritoryOwner.PLAYER_ONE, state.board[7][0].owner)
        assertEquals(TerritoryOwner.PLAYER_TWO, state.board[0][7].owner)
        assertEquals(1, state.player1Score)
        assertEquals(1, state.player2Score)
        assertEquals(62, state.neutralCount)
        assertEquals(Player.PLAYER_ONE, state.currentPlayer)
        // Terrain is preserved under ownership.
        assertEquals(BoardLayout.terrainAt(7, 0), state.board[7][0].terrainColor)
        assertTrue(state.legalColors.isNotEmpty())
    }

    @Test
    fun fixedLayoutIsBalanced() {
        val b = TerritoryEngine.createInitialBoard()
        val counts = TerrainColor.entries.associateWith { color -> b.sumOf { row -> row.count { it.terrainColor == color } } }
        counts.values.forEach { assertTrue("colour count $it", it in 11..15) }
        assertTrue(TerritoryEngine.getLegalColors(b, Player.PLAYER_ONE).size >= 2)
        assertTrue(TerritoryEngine.getLegalColors(b, Player.PLAYER_TWO).size >= 2)
    }

    @Test
    fun orthogonalNeighbors() {
        assertEquals(
            setOf(GridPosition(2, 3), GridPosition(4, 3), GridPosition(3, 2), GridPosition(3, 4)),
            TerritoryEngine.getNeighbors(GridPosition(3, 3)).toSet()
        )
        assertEquals(
            setOf(GridPosition(1, 0), GridPosition(0, 1)),
            TerritoryEngine.getNeighbors(GridPosition(0, 0)).toSet()
        )
        assertEquals(
            setOf(GridPosition(6, 7), GridPosition(7, 6)),
            TerritoryEngine.getNeighbors(GridPosition(7, 7)).toSet()
        )
    }

    @Test
    fun singleCellExpansion() {
        // Row 7 col 1 is Plum and isolated from other Plum by Sand on every side.
        val terrain = List(7) { "SSSSSSSS" } + "SPSSSSSS"
        val b = board(terrain, owners(p1Start to '1', p2Start to '2'))
        assertEquals(setOf(GridPosition(7, 1)), TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, PLUM))
    }

    @Test
    fun multiCellFloodExpansion() {
        // Sage path snaking away from the bottom-left corner.
        val terrain = listOf(
            "SSSSSSSS",
            "SSSSSSSS",
            "SSSSSSSS",
            "SSSSSSSS",
            "SSGGGSSS",
            "SSGSSSSS",
            "SSGSSSSS",
            "BGGSSSSS"
        )
        val b = board(terrain, owners(p1Start to '1', p2Start to '2'))
        val cells = TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, SAGE)
        assertEquals(
            setOf(
                GridPosition(7, 1), GridPosition(7, 2), GridPosition(6, 2), GridPosition(5, 2),
                GridPosition(4, 2), GridPosition(4, 3), GridPosition(4, 4)
            ),
            cells
        )
        val layers = TerritoryEngine.getExpansionLayers(b, Player.PLAYER_ONE, SAGE)
        assertEquals(setOf(GridPosition(7, 1)), layers.first())
        assertEquals(cells.size, layers.sumOf { it.size })
    }

    @Test
    fun diagonalCellIsNotExpanded() {
        // Only (6,1) is Sage — diagonal to the corner, so no expansion.
        val terrain = List(6) { "SSSSSSSS" } + "SGSSSSSS" + "BSSSSSSS"
        val b = board(terrain, owners(p1Start to '1', p2Start to '2'))
        assertTrue(TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, SAGE).isEmpty())
        assertFalse(SAGE in TerritoryEngine.getLegalColors(b, Player.PLAYER_ONE))
    }

    @Test
    fun differentColorStopsExpansion() {
        // Sage at (7,1), Plum at (7,2), Sage again at (7,3): flood must stop at the Plum.
        val terrain = List(7) { "SSSSSSSS" } + "BGPGSSSS"
        val b = board(terrain, owners(p1Start to '1', p2Start to '2'))
        assertEquals(setOf(GridPosition(7, 1)), TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, SAGE))
    }

    @Test
    fun opponentTerritoryBlocksExpansion() {
        // Sage row along the bottom, but Player 2 owns (7,3) in the middle of it.
        val terrain = List(7) { "SSSSSSSS" } + "BGGGGGSS"
        val b = board(terrain, owners(p1Start to '1', p2Start to '2', GridPosition(7, 3) to '2'))
        val cells = TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, SAGE)
        assertEquals(setOf(GridPosition(7, 1), GridPosition(7, 2)), cells)
        assertFalse(GridPosition(7, 3) in cells)

        // Applying the move never changes the opponent's tile.
        val state = TerritoryEngine.newGame(b)
        val after = TerritoryEngine.applyColor(state, SAGE)!!
        assertEquals(TerritoryOwner.PLAYER_TWO, after.board[7][3].owner)
        assertEquals(TerritoryOwner.NONE, after.board[7][4].owner)
    }

    @Test
    fun ownedCellsAreStartingBoundaries() {
        // Player 1 owns two separated cells; both seed the search.
        val terrain = List(8) { "SSSSSSSS" }.toMutableList().apply {
            this[7] = "BGSSSSSS"
            this[3] = "SSSSGBSS"
        }
        val b = board(terrain, owners(p1Start to '1', GridPosition(3, 5) to '1', p2Start to '2'))
        assertEquals(
            setOf(GridPosition(7, 1), GridPosition(3, 4)),
            TerritoryEngine.getExpansionCells(b, Player.PLAYER_ONE, SAGE)
        )
    }

    @Test
    fun legalColorDetection() {
        val terrain = List(7) { "SSSSSSSS" } + "BGPSSSSS"
        val b = board(terrain, owners(p1Start to '1', p2Start to '2'))
        // Corner (7,0) touches (6,0)=Sand and (7,1)=Sage only.
        assertEquals(setOf(SAND, SAGE), TerritoryEngine.getLegalColors(b, Player.PLAYER_ONE))
    }

    @Test
    fun illegalColorIsRejected() {
        val state = TerritoryEngine.newGame(board(striped, owners(p1Start to '1', p2Start to '2')))
        val illegal = TerrainColor.entries.first { it !in state.legalColors }
        assertNull(TerritoryEngine.applyColor(state, illegal))
    }

    @Test
    fun scoreCalculationAndTurnSwitch() {
        val terrain = List(7) { "SSSSSSSS" } + "BGGGSSSS"
        val state = TerritoryEngine.newGame(board(terrain, owners(p1Start to '1', p2Start to '2')))
        val after = TerritoryEngine.applyColor(state, SAGE)!!
        assertEquals(4, after.player1Score)
        assertEquals(1, after.player2Score)
        assertEquals(59, after.neutralCount)
        assertEquals(1, after.moveCount)
        assertEquals(Player.PLAYER_TWO, after.currentPlayer)
        assertEquals(setOf(GridPosition(7, 1), GridPosition(7, 2), GridPosition(7, 3)), after.lastExpansion)
        assertEquals(TerritoryEngine.getLegalColors(after.board, Player.PLAYER_TWO), after.legalColors)
    }

    @Test
    fun sameColorCanBeReused() {
        var state = TerritoryEngine.newGame()
        val first = state.legalColors.first()
        state = TerritoryEngine.applyColor(state, first)!!
        // Play until Player 1 can choose `first` again, or the game ends.
        var reused = false
        while (!state.isGameOver && !reused) {
            if (state.legalColors.isEmpty()) { state = TerritoryEngine.applyPass(state); continue }
            if (state.currentPlayer == Player.PLAYER_ONE && first in state.legalColors) {
                state = TerritoryEngine.applyColor(state, first)!!
                reused = true
            } else {
                state = TerritoryEngine.applyColor(state, state.legalColors.first())!!
            }
        }
        assertTrue(reused)
    }

    @Test
    fun automaticPassSwitchesPlayer() {
        // Player 1 at the corner is walled in by Player 2 tiles.
        val b = board(
            striped,
            owners(p1Start to '1', p2Start to '2', GridPosition(6, 0) to '2', GridPosition(7, 1) to '2')
        )
        val state = TerritoryEngine.newGame(b)
        assertTrue(state.legalColors.isEmpty())
        val passed = TerritoryEngine.applyPass(state)
        assertEquals(1, passed.consecutivePasses)
        assertEquals(Player.PLAYER_TWO, passed.currentPlayer)
        assertFalse(passed.isGameOver)
        assertTrue(passed.legalColors.isNotEmpty())

        // A successful expansion resets the pass counter.
        val moved = TerritoryEngine.applyColor(passed, passed.legalColors.first())!!
        assertEquals(0, moved.consecutivePasses)
    }

    @Test
    fun twoConsecutivePassesEndGame() {
        val state = ColorTerritoryState(
            board = board(striped, owners(p1Start to '1', p2Start to '2', GridPosition(0, 6) to '2')),
            currentPlayer = Player.PLAYER_ONE,
            legalColors = emptySet(),
            player1Score = 1,
            player2Score = 2
        )
        val once = TerritoryEngine.applyPass(state)
        assertFalse(once.isGameOver)
        val twice = TerritoryEngine.applyPass(once.copy(legalColors = emptySet()))
        assertTrue(twice.isGameOver)
        assertEquals(2, twice.consecutivePasses)
        assertEquals(Player.PLAYER_TWO, twice.winner)
        assertTrue(twice.legalColors.isEmpty())
        // No further actions after the game ends.
        assertNull(TerritoryEngine.applyColor(twice, SAND))
        assertEquals(twice, TerritoryEngine.applyPass(twice))
    }

    @Test
    fun fullBoardEndsGame() {
        // Everything owned except (7,1), which Player 1 takes to fill the board.
        val ownerRows = List(8) { r -> if (r < 3) "22222222" else "11111111" }.toMutableList()
        ownerRows[7] = "1.111111"
        val terrain = List(7) { "SSSSSSSS" } + "BGSSSSSS"
        val state = TerritoryEngine.newGame(board(terrain, ownerRows))
        assertEquals(setOf(SAGE), state.legalColors)
        val end = TerritoryEngine.applyColor(state, SAGE)!!
        assertEquals(64, end.player1Score + end.player2Score)
        assertEquals(0, end.neutralCount)
        assertTrue(end.isGameOver)
        assertEquals(Player.PLAYER_ONE, end.winner)
        assertFalse(end.isDraw)
    }

    @Test
    fun winnerDetermination() {
        val ownerRows = List(8) { r -> if (r < 3) "22222222" else "11111111" }
        val state = TerritoryEngine.finishGame(ColorTerritoryState(board(striped, ownerRows)))
        assertEquals(40, state.player1Score)
        assertEquals(24, state.player2Score)
        assertEquals(Player.PLAYER_ONE, state.winner)

        val flipped = List(8) { r -> if (r < 5) "22222222" else "11111111" }
        assertEquals(Player.PLAYER_TWO, TerritoryEngine.finishGame(ColorTerritoryState(board(striped, flipped))).winner)
    }

    @Test
    fun drawOnEqualTerritory() {
        val ownerRows = List(8) { r -> if (r < 4) "22222222" else "11111111" }
        val state = TerritoryEngine.finishGame(ColorTerritoryState(board(striped, ownerRows)))
        assertTrue(state.isDraw)
        assertNull(state.winner)
        assertTrue(state.isGameOver)
    }

    @Test
    fun undoSnapshotRestoresFullState() {
        val start = TerritoryEngine.newGame()
        val history = ArrayDeque<ColorTerritoryState>()
        history.addLast(start)
        val afterMove = TerritoryEngine.applyColor(start, start.legalColors.first())!!
        assertNotNull(afterMove)
        val restored = history.removeLast()
        assertEquals(start, restored)
        assertEquals(Player.PLAYER_ONE, restored.currentPlayer)
        assertEquals(0, restored.moveCount)
        assertEquals(1, restored.player1Score)
    }

    @Test
    fun fullGameAlwaysTerminatesWithFilledBoard() {
        // Greedy self-play on the real layout: the game must end, scores must add up.
        var state = TerritoryEngine.newGame()
        var steps = 0
        while (!state.isGameOver && steps < 200) {
            state = if (state.legalColors.isEmpty()) {
                TerritoryEngine.applyPass(state)
            } else {
                val best = state.legalColors.maxBy {
                    TerritoryEngine.getExpansionCells(state.board, state.currentPlayer, it).size
                }
                TerritoryEngine.applyColor(state, best)!!
            }
            val (p1, p2) = TerritoryEngine.scores(state.board)
            assertEquals(p1, state.player1Score)
            assertEquals(p2, state.player2Score)
            steps++
        }
        assertTrue(state.isGameOver)
        assertEquals(64, state.player1Score + state.player2Score)
    }
}
