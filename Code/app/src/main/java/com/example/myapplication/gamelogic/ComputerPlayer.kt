package com.example.myapplication.gamelogic

import kotlin.random.Random

class ComputerPlayer {
    private val untriedCells = mutableListOf<Pair<Int, Int>>()
    private val targetQueue = mutableListOf<Pair<Int, Int>>()
    private var lastHitCell: Pair<Int, Int>? = null
    private val recentHits = mutableListOf<Pair<Int, Int>>()

    init {
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                untriedCells.add(row to col)
            }
        }
    }
    fun placeGeckosRandomly(board: Board) {
        val geckoSizes = listOf(2, 3, 4)
        var geckoId = 0

        for (size in geckoSizes) {
            var placed = false
            var attempts = 0

            while (!placed && attempts < 100) {
                val orientation = if (Random.nextBoolean()) Orientation.HORIZONTAL else Orientation.VERTICAL

                if (size == 4) {
                    val row = Random.nextInt(0, 9)
                    val col = Random.nextInt(0, 9)

                    val cells = listOf(
                        row to col,
                        row to col + 1,
                        row + 1 to col,
                        row + 1 to col + 1
                    )

                    if (cells.all { (r, c) -> !board.grid[r][c].hasGecko }) {
                        val gecko = Gecko("Computer_G$geckoId", size, Orientation.VERTICAL)

                        cells.forEach { (r, c) ->
                            val cell = board.grid[r][c]
                            cell.hasGecko = true
                            gecko.positions.add(cell)
                        }

                        board.geckos.add(gecko)
                        placed = true
                        geckoId++
                    }
                } else {
                    val maxRow = if (orientation == Orientation.VERTICAL) 10 - size else 9
                    val maxCol = if (orientation == Orientation.HORIZONTAL) 10 - size else 9

                    val startRow = Random.nextInt(0, maxRow + 1)
                    val startCol = Random.nextInt(0, maxCol + 1)

                    placed = board.placeGecko(
                        Gecko("Computer_G$geckoId", size, orientation),
                        startRow,
                        startCol
                    )

                    if (placed) geckoId++
                }

                attempts++
            }
        }
    }
    fun decideNextAttack(): Pair<Int, Int> {
        if (targetQueue.isNotEmpty()) {
            val target = targetQueue.removeAt(0)
            untriedCells.remove(target)
            return target
        }

        val randomIndex = Random.nextInt(untriedCells.size)
        val target = untriedCells.removeAt(randomIndex)
        return target
    }
    fun processAttackResult(row: Int, col: Int, wasHit: Boolean, geckoDefeated: Boolean) {
        if (wasHit) {
            lastHitCell = row to col
            recentHits.add(row to col)

            if (!geckoDefeated) {
                addAdjacentCellsToQueue(row, col)
            } else {
                recentHits.clear()
                targetQueue.clear()
            }
        }
    }
    private fun addAdjacentCellsToQueue(row: Int, col: Int) {
        val adjacent = listOf(
            row - 1 to col,
            row + 1 to col,
            row to col - 1,
            row to col + 1
        )

        for (cell in adjacent) {
            val (r, c) = cell
            if (r in 0..9 && c in 0..9 && cell in untriedCells && cell !in targetQueue) {
                targetQueue.add(cell)
            }
        }
    }
}